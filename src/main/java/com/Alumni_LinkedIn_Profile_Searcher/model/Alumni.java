package com.Alumni_LinkedIn_Profile_Searcher.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "alumni_profiles")
public class Alumni {
    @Id
    private Long id;

    @Column("name")
    private String name;

    @Column("current_alumni_role")
    private String currentRole;

    @Column("university")
    private String university;
    @Column("location")
    private String location;
    @Column("linkedinHeadline")
    private String linkedinHeadline;
    @Column("passoutYear")
    private Integer passoutYear;
}
