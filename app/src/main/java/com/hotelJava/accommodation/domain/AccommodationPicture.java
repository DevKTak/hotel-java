package com.hotelJava.accommodation.domain;

import com.hotelJava.common.embeddable.PictureInfo;
import com.hotelJava.common.util.BaseTimeEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AccommodationPicture extends BaseTimeEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Embedded
    private PictureInfo pictureInfo;

    @OneToOne(mappedBy = "accommodationPicture")
    private Accommodation accommodation;
}
