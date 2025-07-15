package SingSongGame.BE.chat.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WebSocketResponse {
    private String type;
    private Object data;
}

