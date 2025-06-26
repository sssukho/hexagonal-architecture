package com.sssukho.api.unit.service;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sssukho.api.service.DealService;
import com.sssukho.api.service.MemberService;
import com.sssukho.api.service.RoomService;
import com.sssukho.common.dto.room.DealTypeDto;
import com.sssukho.common.dto.room.RoomRegistrationRequest;
import com.sssukho.common.dto.room.RoomRegistrationRequest.DealRegistrationRequest;
import com.sssukho.common.dto.room.RoomResponse;
import com.sssukho.common.dto.room.RoomTypeDto;
import com.sssukho.common.dto.room.RoomUpdateRequest;
import com.sssukho.common.dto.room.RoomUpdateRequest.DealUpdateRequest;
import com.sssukho.domain.deal.Deal;
import com.sssukho.domain.member.Member;
import com.sssukho.domain.room.Room;
import com.sssukho.domain.room.RoomRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class RoomEntityServiceTest {

    @InjectMocks
    private RoomService roomService;

    @Mock
    private MemberService memberService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private DealService dealService;

    @DisplayName("내방 등록 성공 테스트")
    @Test
    void testRegisterMyRoomSucceed() {
        // given
        RoomRegistrationRequest fakeRequest = new RoomRegistrationRequest("방 제목", "방 설명",
            "서울시 서초구 서초대로 301 동익 성봉빌딩 10층 (주)스테이션3", 33.5, RoomTypeDto.ONE_ROOM, List.of(
            new DealRegistrationRequest(DealTypeDto.MONTHLY_RENT, BigDecimal.valueOf(2000),
                BigDecimal.valueOf(50))));

        final String fakeMemberEmail = "dev.sssukho@gmail.com";

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(fakeMemberEmail);
        SecurityContextHolder.setContext(securityContext);

        Member fakeMember = Member.createMemberWithId(1L, fakeMemberEmail, "password", "임석호", null);
        when(memberService.findByEmail(any())).thenReturn(fakeMember);

        final long fakeMemberId = 123L;
        Room fakeRegisteredRoom = Room.of(1L, fakeRequest.title(), fakeRequest.description(),
            fakeRequest.address(), fakeRequest.area(), fakeRequest.roomType(), fakeMemberId,
            LocalDateTime.now(), LocalDateTime.now());
        Deal fakeDealInRegisteredRoom = Deal.of(fakeRequest.deals().get(0).dealType(),
            fakeRequest.deals().get(0).deposit(), fakeRequest.deals().get(0).monthlyRent(),
            fakeMemberId);
        fakeRegisteredRoom.setDeals(List.of(fakeDealInRegisteredRoom));

        when(roomRepository.save(any())).thenReturn(fakeRegisteredRoom);

        doNothing().when(dealService).registerAll(anyList());

        // when
        RoomResponse result = roomService.register(fakeRequest);

        // then
        assertEquals(fakeRegisteredRoom.getId(), result.id());
        assertEquals(fakeRequest.title(), result.title());
        assertEquals(fakeRequest.description(), result.description());
        assertEquals(fakeRequest.address(),result.address());

        verify(roomRepository, times(1)).save(any(Room.class));
        verify(dealService, times(1)).registerAll(anyList());
        verify(memberService, times(1)).findByEmail(fakeMemberEmail);
    }

    @DisplayName("내방 삭제 성공 테스트")
    @Test
    void testDeleteMyRoomMyRoomSucceed() {
        // given
        final long fakeRoomId = 1L;
        final String fakeMemberEmail = "dev.sssukho@gmail.com";
        final long fakeMemberId = 5L;

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(fakeMemberEmail);
        SecurityContextHolder.setContext(securityContext);

        Member fakeMemberDomain = Member.createMemberWithId(fakeMemberId, fakeMemberEmail, "password", "임석호", null);
        when(memberService.findByEmail(any())).thenReturn(fakeMemberDomain);

        Room fakeFoundRoom = Room.of(fakeRoomId, "타이틀1", "설명1", "주소1", 3.14, RoomTypeDto.TWO_ROOM,
            fakeMemberId, LocalDateTime.now(), LocalDateTime.now());
        Deal fakeDealInFoundRoom = Deal.of(DealTypeDto.MONTHLY_RENT, BigDecimal.valueOf(2000),
            BigDecimal.valueOf(50), fakeFoundRoom.getId());
        fakeFoundRoom.setDeals(List.of(fakeDealInFoundRoom));

        when(roomRepository.findById(fakeRoomId)).thenReturn(fakeFoundRoom);

        // when & then
        assertDoesNotThrow(() -> roomService.deleteMyRoom(fakeRoomId));

        verify(roomRepository, times(1)).findById(any());
        verify(roomRepository, times(1)).deleteById(any());
    }

    @DisplayName("내방 수정 성공 테스트")
    @Test
    void testUpdateMyRoomMyRoomSucceed() {
        // given
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("dev.sssukho@gmail.com");
        SecurityContextHolder.setContext(securityContext);

        final long fakeMemberId = 123L;
        Member fakeMemberDomain = Member.createMemberWithId(fakeMemberId, "dev.sssukho@gmail.com", "password", "임석호", null);
        when(memberService.findByEmail(any())).thenReturn(fakeMemberDomain);

        final long fakeRoomIdToUpdate = 1L;
        List<DealUpdateRequest> fakeDealUpdateRequests = List.of(
            new DealUpdateRequest(DealTypeDto.MONTHLY_RENT, BigDecimal.valueOf(2000),
                BigDecimal.valueOf(150)),
            new DealUpdateRequest(DealTypeDto.YEAR_RENT, BigDecimal.valueOf(500000000),
                BigDecimal.valueOf(10))
        );
        RoomUpdateRequest fakeRequest = new RoomUpdateRequest("수정된 타이틀", "수정된 설명", "수정된 주소", 45.0,
            RoomTypeDto.THREE_ROOM, fakeDealUpdateRequests);

        Room fakeFoundRoom = Room.of(fakeRoomIdToUpdate, fakeRequest.title(),
            fakeRequest.description(), fakeRequest.address(), fakeRequest.area(),
            fakeRequest.roomType(), fakeMemberId, LocalDateTime.now(), LocalDateTime.now());
        Deal fakeDealInFoundRoom = Deal.of(DealTypeDto.MONTHLY_RENT, BigDecimal.valueOf(2000),
            BigDecimal.valueOf(50), fakeFoundRoom.getId());
        fakeFoundRoom.setDeals(List.of(fakeDealInFoundRoom));

        when(roomRepository.findById(fakeRoomIdToUpdate)).thenReturn(fakeFoundRoom);

        when(roomRepository.update(fakeFoundRoom)).thenReturn(fakeFoundRoom);
        doNothing().when(dealService).deleteByRoom(fakeFoundRoom);
        doNothing().when(dealService).registerAll(anyList());

        // when
        RoomResponse result = roomService.updateMyRoom(fakeRoomIdToUpdate, fakeRequest);

        // then
        assertEquals(fakeRequest.title(), result.title());
        assertEquals(fakeRequest.description(),result.description());
        assertEquals(fakeRequest.address(),result.address());
        assertEquals(fakeRequest.area(),result.area());
        assertEquals(fakeRequest.roomType(), result.roomType());

        verify(roomRepository).findById(fakeRoomIdToUpdate);
        verify(dealService).deleteByRoom(fakeFoundRoom);
        verify(dealService).registerAll(anyList());
    }

    @DisplayName("내방 단건 조회 성공 테스트")
    @Test
    void testFindMyRoomMyRoomSucceed() {
        // given
        final String fakeMemberEmail = "dev.sssukho@gmail.com";
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(fakeMemberEmail);
        SecurityContextHolder.setContext(securityContext);

        final long fakeMemberId = 123L;
        Member mockedMember = mock(Member.class);
        when(mockedMember.getId()).thenReturn(fakeMemberId);
        when(mockedMember.getEmail()).thenReturn(fakeMemberEmail);

        when(memberService.findByEmail(fakeMemberEmail)).thenReturn(mockedMember);

        final long fakeRoomId = 12L;
        Room fakeFoundRoom = Room.of(fakeRoomId, "타이틀", "설명", "주소", 3.14, RoomTypeDto.ONE_ROOM,
            fakeMemberId, LocalDateTime.now(), LocalDateTime.now());
        Deal fakeDealInFoundRoom = Deal.of(DealTypeDto.MONTHLY_RENT, BigDecimal.valueOf(2000),
            BigDecimal.valueOf(50), fakeFoundRoom.getId());
        fakeFoundRoom.setDeals(List.of(fakeDealInFoundRoom));

        when(roomRepository.findById(fakeRoomId)).thenReturn(fakeFoundRoom);

        // when
        RoomResponse result = roomService.findMyRoom(fakeRoomId);

        // then
        assertEquals(fakeFoundRoom.getId(), result.id());
        assertEquals(fakeFoundRoom.getRoomTypeDto(), result.roomType());

        verify(memberService).findByEmail(fakeMemberEmail);
        verify(roomRepository).findById(fakeRoomId);
    }

    @DisplayName("내방 목록 조회 성공 테스트")
    @Test
    void testFindMyRoomMyRoomsSucceed() {
        // given
        final String fakeMemberEmail = "dev.sssukho@gmail.com";
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(fakeMemberEmail);
        SecurityContextHolder.setContext(securityContext);

        Member mockedFakeMember = mock(Member.class);

        when(memberService.findByEmail(fakeMemberEmail)).thenReturn(mockedFakeMember);
        final long fakeMemberId = 123L;
        when(mockedFakeMember.getId()).thenReturn(fakeMemberId);

        Room fakeFoundRoom1 = Room.of(1L, "타이틀1", "설명1", "주소1", 3.14, RoomTypeDto.TWO_ROOM,
            fakeMemberId, LocalDateTime.now(), LocalDateTime.now());
        Deal fakeDealInFoundRoom1 = Deal.of(DealTypeDto.MONTHLY_RENT, BigDecimal.valueOf(2000),
            BigDecimal.valueOf(50), fakeFoundRoom1.getId());
        fakeFoundRoom1.setDeals(List.of(fakeDealInFoundRoom1));

        Room fakeFoundRoom2 = Room.of(2L, "타이틀2", "설명2", "주소2", 3.15, RoomTypeDto.ONE_ROOM,
            fakeMemberId, LocalDateTime.now(), LocalDateTime.now());
        Deal fakeDealInFoundRoom2 = Deal.of(DealTypeDto.MONTHLY_RENT, BigDecimal.valueOf(2000),
            BigDecimal.valueOf(50), fakeFoundRoom2.getId());
        fakeFoundRoom2.setDeals(List.of(fakeDealInFoundRoom2));

        List<Room> fakeFoundMyRooms = List.of(fakeFoundRoom1, fakeFoundRoom2);

        when(roomRepository.findAllByOwnerId(fakeMemberId)).thenReturn(fakeFoundMyRooms);
        when(dealService.findAllDealsByRoomIds(
            List.of(fakeFoundRoom1.getId(), fakeFoundRoom2.getId()))).thenReturn(
            List.of(fakeDealInFoundRoom1, fakeDealInFoundRoom2));

        // when
        List<RoomResponse> result = roomService.findMyRooms();

        // then
        assertEquals(fakeFoundMyRooms.get(0).getId(),result.get(0).id());
        assertEquals(fakeFoundMyRooms.get(1).getId(),result.get(1).id());

        verify(memberService).findByEmail(fakeMemberEmail);
        verify(roomRepository).findAllByOwnerId(fakeMemberId);
    }
}
