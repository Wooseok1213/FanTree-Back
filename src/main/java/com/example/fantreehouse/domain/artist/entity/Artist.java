package com.example.fantreehouse.domain.artist.entity;

import com.example.fantreehouse.common.entitiy.Timestamped;
import com.example.fantreehouse.domain.artistgroup.entity.ArtistGroup;
import com.example.fantreehouse.domain.entertainment.entity.Entertainment;
import com.example.fantreehouse.domain.feed.entity.Feed;
import com.example.fantreehouse.domain.subscription.entity.Subscription;
import com.example.fantreehouse.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "artist")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Artist extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String artistName; //활동명

    private int rank;

    private Long subscriberCount;

    @Column(nullable = false)
    private String artistProfilePicture;

    @ManyToOne
    @JoinColumn(name = "artist_group_id")
    private ArtistGroup artistGroup;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}


