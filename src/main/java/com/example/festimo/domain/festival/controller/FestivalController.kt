package com.example.festimo.domain.festival.controller

import com.example.festimo.domain.festival.dto.FestivalTO
import com.example.festimo.domain.festival.service.FestivalService
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@Tag(name = "축제 API", description = "축제 관련 API")
class FestivalController(
    private val festivalService: FestivalService,
    private val pagedResourcesAssembler: PagedResourcesAssembler<FestivalTO>,
    @Value("\${KAKAO_MAP_API_KEY}") private val KAKAO_MAP_API_KEY: String
) {

    @ResponseBody
    @GetMapping("/api/map-key")
    @Hidden
    fun getApiKey(): String = KAKAO_MAP_API_KEY

    // 수동으로 축제 api를 불러올 수 있는 방법
    @GetMapping("/manuallyGetAllEvents")
    @ResponseBody
    fun manuallyGetAllEvents(): ResponseEntity<String> {
        festivalService.refreshEvents()
        return ResponseEntity.ok("모든 축제 api를 성공적으로 불러왔습니다")
    }

    @ResponseBody
    @GetMapping("/api/events")
    @Operation(summary = "전체 축제 조회")
    fun getAllEvents(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "28") size: Int,
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) keyword: String?
    ): PagedModel<FestivalTO> {
        return try {
            val pageable: Pageable = PageRequest.of(page, size)

            val paginatedEvent: Page<FestivalTO> = when {
                year != null && month != null -> festivalService.filterByMonth(year, month, pageable)
                region != null -> festivalService.filterByRegion(region, pageable)
                keyword != null && keyword.isNotEmpty() -> festivalService.search(keyword, pageable)
                else -> festivalService.findPaginated(pageable)
            }

            val pagedModel = pagedResourcesAssembler.toModel(paginatedEvent) { festival -> EntityModel.of(festival) }
            PagedModel.of(pagedModel.content.map { it.content }, pagedModel.metadata)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("전체 축제 조회 중 문제가 발생했습니다.", e)
        }
    }

    @ResponseBody
    @GetMapping("/api/events/{eventId}")
    @Operation(summary = "축제 상세 조회")
    fun getEvent(@PathVariable eventId: Int): FestivalTO? {
        return festivalService.findById(eventId)
    }

    @ResponseBody
    @GetMapping("/api/events/search")
    @Operation(summary = "축제 검색")
    fun search(
        @RequestParam keyword: String,
        @RequestParam page: Int,
        @RequestParam size: Int
    ): Page<FestivalTO> {
        val pageable: Pageable = PageRequest.of(page, size)
        return festivalService.search(keyword, pageable)
    }

    @ResponseBody
    @GetMapping("/api/events/filter/month")
    @Operation(summary = "축제 날짜별 필터링")
    fun filterByMonth(
        @RequestParam year: Int,
        @RequestParam month: Int,
        @RequestParam page: Int,
        @RequestParam size: Int
    ): Page<FestivalTO> {
        val pageable: Pageable = PageRequest.of(page, size)
        return festivalService.filterByMonth(year, month, pageable)
    }

    @ResponseBody
    @GetMapping("/api/events/filter/region")
    @Operation(summary = "축제 지역별 필터링")
    fun filterByRegion(
        @RequestParam region: String,
        @RequestParam page: Int,
        @RequestParam size: Int
    ): Page<FestivalTO> {
        val pageable: Pageable = PageRequest.of(page, size)
        return festivalService.filterByRegion(region, pageable)
    }
}
