package com.hotelJava.reservation.application.service;

import com.hotelJava.common.error.ErrorCode;
import com.hotelJava.common.error.exception.BadRequestException;
import com.hotelJava.member.application.port.out.persistence.FindMemberPort;
import com.hotelJava.member.domain.Member;
import com.hotelJava.reservation.adapter.persistence.ReservationRepository;
import com.hotelJava.reservation.application.port.CreateReservationUseCase;
import com.hotelJava.reservation.domain.Reservation;
import com.hotelJava.reservation.domain.ReservationCommand;
import com.hotelJava.reservation.dto.CreateReservationRequest;
import com.hotelJava.reservation.dto.CreateReservationResponse;
import com.hotelJava.reservation.util.RandomReservationNumberGenerator;
import com.hotelJava.room.adapter.persistence.RoomRepository;
import com.hotelJava.room.domain.Room;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 결제보다 예약을 먼저 처리하고 재고를 선점하는 방식의 예약 클래스
 */
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class EagerReservationService implements CreateReservationUseCase {

    private final RoomRepository roomRepository;
    private final FindMemberPort findMemberPort;
    private final ReservationRepository reservationRepository;
    private final EntityManager entityManager;
    private final RandomReservationNumberGenerator randomReservationNumberGenerator;

    @Override
    public boolean supports(ReservationCommand reservationCommand) {
        return reservationCommand.equals(ReservationCommand.EAGER_RESERVATION);
    }

    @Transactional
    @Override
    public CreateReservationResponse createReservation(
            Long roomId, String email, CreateReservationRequest dto) {
        Room room =
                roomRepository
                        .findByIdWithOptimisticLock(roomId)
                        .orElseThrow(() -> new BadRequestException(ErrorCode.ROOM_NOT_FOUND));



        Member member = findMemberPort.findByEmail(email);

        // 재고 확인
        if (room.isNotEnoughStockAtCheckDate(dto.getCheckDate())) {
            throw new BadRequestException(ErrorCode.OUT_OF_STOCK);
        }

        // 최대 인원수 확인
        if (room.isOverMaxOccupancy(dto.getNumberOfGuests())) {
            throw new BadRequestException(ErrorCode.OVER_MAX_OCCUPANCY);
        }

        // 재고 -1
        room.calcStock(dto.getCheckDate(), -1);
        roomRepository.save(room);

        // 예약
        String reservationNo = randomReservationNumberGenerator.generateReservationNumber();
        Reservation reservation =
                new Reservation(member, room, reservationNo, dto, dto.getPaymentType());
        reservationRepository.save(reservation);

        return new CreateReservationResponse(reservationNo);
    }
}
