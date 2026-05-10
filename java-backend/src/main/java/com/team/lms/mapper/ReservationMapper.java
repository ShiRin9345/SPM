package com.team.lms.mapper;

import com.team.lms.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReservationMapper {
    Reservation selectById(Long id);
    List<Reservation> selectAll();
    List<Reservation> selectByReaderId(Long readerId);
    List<Reservation> selectPendingByBookId(Long bookId);
    Reservation selectPendingByReaderAndBookId(Long readerId, Long bookId);
    List<Reservation> selectPending();
    int insert(Reservation reservation);
    int update(Reservation reservation);
}
