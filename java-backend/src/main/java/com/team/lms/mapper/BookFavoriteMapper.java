package com.team.lms.mapper;

import com.team.lms.entity.BookFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookFavoriteMapper {
    List<BookFavorite> selectByReaderId(Long readerId);

    BookFavorite selectByReaderAndBookId(@Param("readerId") Long readerId, @Param("bookId") Long bookId);

    void insert(BookFavorite favorite);

    void deleteById(Long id);
}
