package SingSongGame.BE.song.presentation;

import SingSongGame.BE.song.application.SongService;
import SingSongGame.BE.song.application.dto.request.SongVerifyRequest;
import SingSongGame.BE.song.application.dto.response.SongResponse;
import SingSongGame.BE.song.application.dto.response.SongVerifyResponse;
import SingSongGame.BE.song.persistence.Song;
import SingSongGame.BE.song.persistence.SongRepository;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;
import SingSongGame.BE.song.persistence.Tag;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/song")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;
    private final SongRepository songRepository;

    @Value("${TTS_KEY_JSON}")
    private String ttsKeyJson;

    @GetMapping("/random")
    public ResponseEntity<SongResponse> getRandomSong() {
        Song song = songService.getRandomSong();
        if (song == null) {
            return ResponseEntity.noContent().build();
        }

        SongResponse response = SongResponse.from(song, null, null); // round는 없으니 null
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<SongVerifyResponse> verifyAnswer(@RequestBody SongVerifyRequest request) {
        return ResponseEntity.ok(songService.verifyAnswer(request));
    }

    @GetMapping(value = "/tts", produces = "audio/mpeg")
    public void getLyricsTtsBySongId(
            @RequestParam("songId") Long songId,
            HttpServletResponse response
    ) {
        // 1. songId로 DB에서 곡 조회
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "곡을 찾을 수 없습니다."));

        String lyrics = song.getLyrics();

        // 🔥 JSON 문자열인지 파일 경로인지 자동 판별
        InputStream keyStream = null;
        try {
            if (ttsKeyJson.trim().startsWith("{")) {
                // JSON 문자열인 경우 (배포 서버)
                keyStream = new ByteArrayInputStream(ttsKeyJson.getBytes(StandardCharsets.UTF_8));
                log.info("🔑 [TTS 키] JSON 문자열에서 로드 (배포 환경)");
            } else {
                // 파일 경로인 경우 (로컬 환경)
                keyStream = new FileInputStream(ttsKeyJson);
                log.info("🔑 [TTS 키] 파일에서 로드 (로컬 환경): {}", ttsKeyJson);
            }

            System.out.println("🔥 GOOGLE_APPLICATION_CREDENTIALS: " + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));

            GoogleCredentials credentials = GoogleCredentials.fromStream(keyStream);
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            // 3. TTS 클라이언트 생성 및 요청 처리
            try (TextToSpeechClient ttsClient = TextToSpeechClient.create(settings)) {
                SynthesisInput input = SynthesisInput.newBuilder()
                        .setText(lyrics)
                        .build();

                VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                        .setLanguageCode("ko-KR")
                        .setName("ko-KR-Wavenet-A")
                        .build();

                AudioConfig audioConfig = AudioConfig.newBuilder()
                        .setAudioEncoding(AudioEncoding.MP3)
                        .build();

                SynthesizeSpeechResponse ttsResponse = ttsClient.synthesizeSpeech(input, voice, audioConfig);
                ByteString audioContents = ttsResponse.getAudioContent();

                // 4. 클라이언트로 MP3 응답
                response.setContentType("audio/mpeg");
                response.setHeader("Content-Disposition", "inline; filename=\"tts.mp3\"");
                response.getOutputStream().write(audioContents.toByteArray());
                response.getOutputStream().flush();

                log.info("✅ [TTS 생성 성공] songId: {}, 가사 길이: {}자", songId, lyrics.length());
            }

        } catch (IOException e) {
            log.error("❌ [TTS 생성 실패] songId: {}, error: {}", songId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "TTS 생성 실패: " + e.getMessage());
        } finally {
            // 스트림 안전하게 닫기
            if (keyStream != null) {
                try {
                    keyStream.close();
                } catch (IOException e) {
                    log.warn("⚠️ [스트림 닫기 실패] {}", e.getMessage());
                }
            }
        }
    }
}
