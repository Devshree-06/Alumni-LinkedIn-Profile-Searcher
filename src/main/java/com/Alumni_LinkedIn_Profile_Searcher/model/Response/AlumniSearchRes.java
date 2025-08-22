package com.Alumni_LinkedIn_Profile_Searcher.model.Response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AlumniSearchRes {
    private String status;
    private String message;
    private List<?> data;
}
