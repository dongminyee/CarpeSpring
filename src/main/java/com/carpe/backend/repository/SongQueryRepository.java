package com.carpe.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.carpe.backend.common.SongStatus;
import com.carpe.backend.dto.SongSearchDto;
import com.carpe.backend.entity.QSong;
import com.carpe.backend.entity.Song;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SongQueryRepository {

    private final JPAQueryFactory queryFactory;

    private final QSong song = QSong.song;

    public Page<Song> search(SongSearchDto dto, Pageable pageable){

        List<Song> content = queryFactory
            .selectFrom(song)
            .where(
                published(),
                keywordContains(dto.keyword()),
                artistContains(dto.artist()),
                titleContains(dto.title())
            )
            .orderBy(getOrderSpecifier(pageable.getSort()))
            .offset(pageable.getOffset())   // 몇 번째부터
            .limit(pageable.getPageSize())  // 몇 개까지
            .fetch();
        
        Long total = queryFactory
            .select(song.count())
            .from(song)
            .where(
                published(),
                keywordContains(dto.keyword()),
                artistContains(dto.artist()),
                titleContains(dto.title())
            )
            .fetchOne();
        return new PageImpl<>(content, pageable, total!=null? total: 0);
    }

    public Page<Song> searchAll(SongSearchDto dto, Pageable pageable){

        List<Song> content = queryFactory
            .selectFrom(song)
            .where(
                keywordContains(dto.keyword()),
                artistContains(dto.artist()),
                titleContains(dto.title())
            )
            .orderBy(getOrderSpecifier(pageable.getSort()))
            .offset(pageable.getOffset())   // 몇 번째부터
            .limit(pageable.getPageSize())  // 몇 개까지
            .fetch();
        
        Long total = queryFactory
            .select(song.count())
            .from(song)
            .where(
                keywordContains(dto.keyword()),
                artistContains(dto.artist()),
                titleContains(dto.title())
            )
            .fetchOne();
        return new PageImpl<>(content, pageable, total!=null? total: 0);
    }

    

    // 조건 메서드들 — null 반환 시 자동으로 WHERE 절에서 제외됨
    private BooleanExpression published() {
        return song.status.eq(SongStatus.PUBLISHED);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return song.title.containsIgnoreCase(keyword)
            .or(song.artist.containsIgnoreCase(keyword));
    }

    private BooleanExpression artistContains(String artist) {
        if (artist == null || artist.isBlank()) return null;
        return song.artist.containsIgnoreCase(artist);
    }

    private BooleanExpression titleContains(String title) {
        if (title == null || title.isBlank()) return null;
        return song.title.containsIgnoreCase(title);
    }

    // Pageable의 Sort 정보를 QueryDSL OrderSpecifier로 변환
    private OrderSpecifier<?> getOrderSpecifier(Sort sort) {
        if (sort.isEmpty()) return song.publishedAt.desc(); // 기본값

        Sort.Order order = sort.iterator().next();
        return switch (order.getProperty()) {
            case "title"       -> order.isAscending() ? song.title.asc()       : song.title.desc();
            case "artist"      -> order.isAscending() ? song.artist.asc()      : song.artist.desc();
            case "publishedAt" -> order.isAscending() ? song.publishedAt.asc() : song.publishedAt.desc();
            case "generation"      -> order.isAscending() ? song.generation.asc()      : song.generation.desc();
            case "concert"      -> order.isAscending() ? song.concert.asc()      : song.concert.desc();
            case "year"      -> order.isAscending() ? song.date.asc()      : song.date.desc();
            default            -> song.publishedAt.desc();
        };
    }
}
