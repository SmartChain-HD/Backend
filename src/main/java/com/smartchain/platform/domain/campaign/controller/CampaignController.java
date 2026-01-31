package com.smartchain.platform.domain.campaign.controller;

import com.smartchain.platform.domain.campaign.service.CampaignService;
import com.smartchain.platform.dto.campaign.detail.CampaignDetailResponse;
import com.smartchain.platform.dto.campaign.list.CampaignListResponse;
import com.smartchain.platform.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaign", description = "캠페인 조회 API")
public class CampaignController {

    private final CampaignService campaignService;

    @Operation(summary = "캠페인 목록 조회", description = "모든 캠페인 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<CampaignListResponse>> getCampaignList() {
        log.info("캠페인 목록 조회 요청");
        CampaignListResponse response = campaignService.getCampaignList();
        return ResponseEntity.ok(BaseResponse.success("캠페인 목록 조회 완료", response));
    }

    @Operation(summary = "캠페인 상세 조회", description = "캠페인 상세 정보를 조회합니다.")
    @GetMapping("/{campaignId}")
    public ResponseEntity<BaseResponse<CampaignDetailResponse>> getCampaignDetail(
            @Parameter(description = "캠페인 ID")
            @PathVariable Long campaignId
    ) {
        log.info("캠페인 상세 조회 요청 - campaignId: {}", campaignId);
        CampaignDetailResponse response = campaignService.getCampaignDetail(campaignId);
        return ResponseEntity.ok(BaseResponse.success("캠페인 상세 조회 완료", response));
    }
}
