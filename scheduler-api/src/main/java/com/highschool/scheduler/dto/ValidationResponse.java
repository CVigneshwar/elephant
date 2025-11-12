package com.highschool.scheduler.dto;

public record ValidationResponse(boolean ok, java.util.List<String> errors) {
}
