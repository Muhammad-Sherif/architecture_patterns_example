package com.charity_hub.domain.helper;


public record CreateOperation<T>(boolean success, T createdData) {
}
