package com.Alumni_LinkedIn_Profile_Searcher.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "alumni_profiles")
@AllArgsConstructor
@NoArgsConstructor
public class Alumni {
    @Id
    private Integer id;

    @Column("name")
    private String name;
    @Column("current_alumni_role")
    private String currentRole;
    @Column("university")
    private String university;
    @Column("alumni_location")
    private String location;
    @Column("linkedin_headline")
    private String linkedinHeadline;
    @Column("passout_year")
    private String passoutYear;
}
