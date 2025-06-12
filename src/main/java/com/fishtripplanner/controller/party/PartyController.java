// PartyController.java
package com.fishtripplanner.controller.party;

import com.fishtripplanner.dto.party.PartyCreateRequest;
import com.fishtripplanner.domain.party.Party;
import com.fishtripplanner.repository.PartyRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/party")
public class PartyController {

    private final PartyRepository partyRepository;
    // + 필요시 Waypoint, PartyMember 등 리포지토리 추가

    // 1차 폼 GET: 파티 모집 1단계 폼 띄우기
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("partyForm", new PartyCreateRequest());
        return "party/create";
    }
    // 1차 폼 POST: 세션에 데이터 저장, 2차 폼 이동
    @PostMapping("/saveRouteInfo")
    public String saveRouteInfo(@ModelAttribute PartyCreateRequest request, HttpSession session) {
        session.setAttribute("partyTemp", request);
        return "redirect:/party/saveDetail";
    }



    // 2차 폼 POST: 입력값 합쳐서 최종 저장
    @PostMapping("/saveDetail")
    public String saveDetail(
            @ModelAttribute PartyCreateRequest detailRequest,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpSession session
    ) {
        PartyCreateRequest routeInfo = (PartyCreateRequest) session.getAttribute("partyTemp");
        if (routeInfo == null) return "redirect:/party/create";

        // 2차 폼 입력값 합치기
        routeInfo.setTitle(detailRequest.getTitle());
        routeInfo.setDepartureDesc(detailRequest.getDepartureDesc());
        routeInfo.setDestinationDesc(detailRequest.getDestinationDesc());
        routeInfo.setWaypointsDesc(detailRequest.getWaypointsDesc());
        routeInfo.setDescription(detailRequest.getDescription());
        routeInfo.setDeadline(detailRequest.getDeadline());
        // 이미지 등은 별도 저장

        // 실제 Party 엔티티 생성 및 저장 (아래 예시는 단순화)
        Party party = Party.builder()
                .title(routeInfo.getTitle())
                .description(routeInfo.getDescription())
                .departurePoint(routeInfo.getDeparturePoint())
                .departureLat(routeInfo.getDepartureLat())
                .departureLng(routeInfo.getDepartureLng())
                .destination(routeInfo.getDestination())
                .destinationLat(routeInfo.getDestinationLat())
                .destinationLng(routeInfo.getDestinationLng())
                .departureTime(routeInfo.getDepartureTime())
                .deadline(routeInfo.getDeadline())
                .build();

        partyRepository.save(party);
        // Waypoint, marineInfo, images 등 추가 저장 필요시 구현

        session.removeAttribute("partyTemp");
        return "redirect:/party/list";
    }
}
