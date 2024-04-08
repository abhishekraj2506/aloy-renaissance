package com.aloy.coreapp.dto;

import com.aloy.coreapp.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RabbitTaskMessageDTO<T> {
    private TaskType type;
    private T data;
}
