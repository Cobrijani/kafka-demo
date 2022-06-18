package com.github.cobrijani.core;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Event<T> {
    private T data;
    private EventAction action;
}
