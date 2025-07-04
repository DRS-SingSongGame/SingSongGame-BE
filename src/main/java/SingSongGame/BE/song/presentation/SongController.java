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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;
import SingSongGame.BE.song.persistence.Tag;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/song")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;
    private final SongRepository songRepository;

    @GetMapping("/random")
    public ResponseEntity<SongResponse> getRandomSong() {
        SongResponse songResponse = songService.getRandomSong();
        if (songResponse == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(songResponse);
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

        try (
                // 2. ClassPathResource로 credentials 파일 읽기
                InputStream keyStream = new ClassPathResource("tts-key.json").getInputStream()
        ) {
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
                        .setName("ko-KR-Wavenet-B")
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
            }

        } catch (IOException e) {
            // 예외 로그 추가 (선택)
            System.err.println("❌ TTS 생성 실패 - credentials 또는 Google API 문제: " + e.getMessage());
            throw new RuntimeException("TTS 생성 실패", e);
        }
    }
}
