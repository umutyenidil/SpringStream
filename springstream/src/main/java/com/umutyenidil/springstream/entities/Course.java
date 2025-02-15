package com.umutyenidil.springstream.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "yt_courses"
)
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    private String id;

    private String title;

    @OneToMany(
            mappedBy = "course"
    )
    private List<Video> list = new ArrayList<>();

}
