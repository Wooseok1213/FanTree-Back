package com.example.fantreehouse.domain.enterfeed.entity;

import com.example.fantreehouse.common.entitiy.Timestamped;
import com.example.fantreehouse.domain.artistgroup.entity.ArtistGroup;
import com.example.fantreehouse.domain.entertainment.entity.Entertainment;
import com.example.fantreehouse.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "enter_feed")
public class EnterFeed extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //엔터테이너먼트와 다대일 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entertainment_id")
    private Entertainment entertainment;

    //아티스트그룹과 다대일 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_group_id")
    private ArtistGroup artistGroup;

    //유저와 다대일 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String contents;

    private String postPicture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedCategory category;

    private LocalDate date;

    public EnterFeed(Entertainment entertainment, ArtistGroup artistGroup, User user, String title, String contents, String postPicture, FeedCategory category, LocalDate  date) {
        this.entertainment = entertainment;
        this.artistGroup = artistGroup;
        this.user = user;
        this.title = title;
        this.contents = contents;
        this.postPicture = postPicture;
        this.category = category;
        this.date = date;
    }

    public void updateContents(String title, String contents, String postPicture, FeedCategory category, LocalDate  date) {
        this.title = title;
        this.contents = contents;
        this.postPicture = postPicture;
        this.category = category;
        this.date = date;
    }
}