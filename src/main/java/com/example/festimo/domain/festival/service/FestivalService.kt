package com.example.festimo.domain.festival.service

import com.example.festimo.domain.festival.domain.Festival
import com.example.festimo.domain.festival.dto.FestivalDetailsTO
import com.example.festimo.domain.festival.dto.FestivalTO
import com.example.festimo.domain.festival.repository.FestivalRepository
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

@Service
open class FestivalService (
    @Value("\${SEARCH_FESTIVAL_API_KEY}") private val SEARCH_FESTIVAL_API_KEY: String,
    @Value("\${INFO_FESTIVAL_API_KEY}") private val INFO_FESTIVAL_API_KEY: String,
    @PersistenceContext private val entityManager: EntityManager,
    private val festivalRepository: FestivalRepository,
    private val redisTemplate: RedisTemplate<String, Object>
) {
    @PostConstruct
    fun init() {
        if (this.redisTemplate == null) {
            throw IllegalStateException("RedisTemplate is not initialized properly.")
        }else{
            println("redisTemplate initialized: ${this.redisTemplate != null}")
        }

        if (this.festivalRepository == null) {
            throw IllegalStateException("festivalRepository is not initialized properly.")
        }else{
            println("festivalRepository initialized: ${this.festivalRepository != null}")
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    fun scheduleRefreshEvents() {
        refreshEvents()
    }

    @Transactional
    open fun refreshEvents() {
        try {
            // 기존 데이터를 삭제하여 데이터 갱신
            festivalRepository!!.deleteAll()

            resetAutoIncrement()

            // API 호출로 데이터 가져오기
            val events = getAllEvents()

            // 가져온 데이터를 데이터베이스에 저장
            for (event in events) {
                insert(event)
            }
        } catch (e: Exception) {
            println("refreshEvents 도중 에러 발생: " + e.message)
        }
    }

    fun getAllEvents(): List<FestivalTO> {
        val factory = DefaultUriBuilderFactory("https://apis.data.go.kr/B551011/KorService1")
        factory.encodingMode = DefaultUriBuilderFactory.EncodingMode.NONE

        val restTemplate = RestTemplate()
        restTemplate.uriTemplateHandler = factory

        val festivalList: MutableList<FestivalTO> = mutableListOf()
        var pageNo = 1
        val numOfRows = 100

        try {
            while (true) {
                val url = factory.expand(
                    "/searchFestival1?serviceKey={serviceKey}&eventStartDate={eventStartDate}" +
                            "&pageNo={pageNo}&numOfRows={numOfRows}&MobileApp={MobileApp}" +
                            "&MobileOS={MobileOS}&listYN={listYN}&arrange={arrange}&_type={_type}",
                    mapOf(
                        "serviceKey" to SEARCH_FESTIVAL_API_KEY,
                        "eventStartDate" to "20230729",
                        "pageNo" to pageNo,
                        "numOfRows" to numOfRows,
                        "MobileApp" to "AppTest",
                        "MobileOS" to "ETC",
                        "listYN" to "Y",
                        "arrange" to "O",
                        "_type" to "json"
                    )
                ).toString()

                val uri = URI(url)
                val responseEntity: ResponseEntity<Map<*, *>> = restTemplate.getForEntity(uri, Map::class.java)
                val response = responseEntity.body as Map<String, Any>

                val body = (response["response"] as Map<String, Any>)["body"] as Map<String, Any>
                val totalCount = body["totalCount"] as Int
                val items = body["items"] as? Map<String, Any>

                if (items == null || !items.containsKey("item")) break

                val itemList = items["item"] as List<Map<String, Any>>

                val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                for (item in itemList) {
                    val title = item["title"] as? String ?: ""
                    val address = (item["addr1"] as? String ?: "") + " " + (item["addr2"] as? String ?: "")

                    val category = when (item["cat2"] as? String) {
                        "A0207" -> "축제"
                        "A0208" -> "행사"
                        else -> "기타"
                    }

                    val startDate = item["eventstartdate"]?.let { LocalDate.parse(it as CharSequence, formatter) }
                    val endDate = item["eventenddate"]?.let { LocalDate.parse(it as CharSequence, formatter) }
                    val image = item["firstimage"] as? String
                    val xCoordinate = (item["mapx"] as? String)?.toFloat() ?: 0f
                    val yCoordinate = (item["mapy"] as? String)?.toFloat() ?: 0f
                    val phone = item["tel"] as? String
                    val contentId = (item["contentid"] as? String)?.toInt() ?: 0

                    val festivalTO = FestivalTO(
                        title = title,
                        category = category,
                        startDate = startDate,
                        endDate = endDate,
                        address = address,
                        image = image,
                        xCoordinate = xCoordinate,
                        yCoordinate = yCoordinate,
                        phone = phone,
                        contentId = contentId,
                        festivalDetails = null
                    )

                    festivalList.add(festivalTO)
                }

                val totalPages = ceil(totalCount.toDouble() / numOfRows).toInt()
                if (pageNo >= totalPages) break
                pageNo++
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
        }

        return festivalList
    }


    fun getFestivalDescription(contentId: Int): FestivalDetailsTO {
        val factory = DefaultUriBuilderFactory("https://apis.data.go.kr/B551011/KorService1").apply {
            encodingMode = DefaultUriBuilderFactory.EncodingMode.NONE
        }

        val restTemplate = RestTemplate().apply {
            uriTemplateHandler = factory
        }

        val details = mutableListOf<FestivalDetailsTO.Detail>()
        try {
            val url = factory.expand(
                "/detailInfo1?serviceKey={serviceKey}&MobileApp={MobileApp}" +
                        "&MobileOS={MobileOS}&contentId={contentId}&contentTypeId={contentTypeId}&_type={_type}",
                mapOf(
                    "serviceKey" to INFO_FESTIVAL_API_KEY,
                    "MobileApp" to "AppTest",
                    "MobileOS" to "ETC",
                    "contentId" to contentId,
                    "contentTypeId" to "15",
                    "_type" to "json"
                )
            ).toString()

            val uri = URI(url)
            val responseEntity = restTemplate.getForEntity(uri, Map::class.java)
            val response = responseEntity.body as Map<*, *>

            val body = (response["response"] as? Map<*, *>)?.get("body") as? Map<*, *>
            val items = body?.get("items")

            val itemList = when (items) {
                is Map<*, *> -> items["item"] as? List<Map<String, Any>>
                is List<*> -> items.filterIsInstance<Map<String, Any>>()
                else -> null
            }

            itemList?.forEach { item ->
                val infoName = item["infoname"] as? String ?: ""
                val infoText = item["infotext"] as? String ?: ""
                details.add(FestivalDetailsTO.Detail(infoName, infoText, contentId))
            }
        } catch (e: Exception) {
            println("Error fetching description: ${e.message}")
        }

        return FestivalDetailsTO(details)
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun resetAutoIncrement() {
        entityManager!!.createNativeQuery("ALTER TABLE festival AUTO_INCREMENT = 1").executeUpdate()
    }

    @Transactional
    open fun insert(to: FestivalTO?) {
        val modelMapper = ModelMapper()
        val festival = modelMapper.map(to, Festival::class.java)

        festivalRepository!!.save(festival)
    }


    open fun findPaginated(pageable: Pageable?): Page<FestivalTO> {
        val festivals = festivalRepository!!.findAll(pageable)
        val modelMapper = ModelMapper()
        val page = festivals.map { festival: Festival? ->
            modelMapper.map(
                festival,
                FestivalTO::class.java
            )
        }
        return page
    }

    open fun findPaginatedWithCache(pageable: Pageable): Page<FestivalTO> {
        val cacheKey = "festivals:page:" + pageable.pageNumber + ":" + pageable.pageSize
        val totalElementsKey = "festivals:totalElements"

        // 1. 캐시된 페이지 데이터 확인
        val cachedData = redisTemplate!!.opsForValue()[cacheKey]
        if (cachedData != null) {
            try {
                val objectMapper = ObjectMapper()
                objectMapper.registerModule(JavaTimeModule())
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

                // List<FestivalTO>로 캐시 된 데이터 Deserialize
                val cachedList: List<FestivalTO> = objectMapper.convertValue(
                    cachedData,
                    object : TypeReference<List<FestivalTO>>() {}  // List<FestivalTO> 타입으로 명확히 지정
                )

                // 2. 캐시된 totalElements 확인
                val totalElementsStr = redisTemplate!!.opsForValue()[totalElementsKey] as String
                var totalElements = if (totalElementsStr != null) {
                    try {
                        totalElementsStr.toLong()
                    } catch (e: NumberFormatException) {
                        // 숫자 형식이 아니면 DB에서 조회
                        festivalRepository!!.count()
                    }
                } else {
                    // 캐시에 값이 없으면 DB에서 조회
                    festivalRepository!!.count()
                }

                return PageImpl(cachedList, pageable, totalElements)
            } catch (e: Exception) {
                System.err.println("Failed to deserialize cached data: " + e.message)
                redisTemplate!!.delete(cacheKey)
            }
        }

        // 3. 캐시가 없는 경우 DB에서 조회
        val page = findPaginated(pageable)
        println("Retrieved page: " + page.content)

        // 4. 페이지 데이터와 전체 개수를 캐시에 저장
        redisTemplate!!.opsForValue().set(cacheKey, page.content as Object, Duration.ofHours(24))
        redisTemplate!!.opsForValue().set(totalElementsKey, page.totalElements.toString() as Object, Duration.ofHours(24))

        return page
    }

    @Transactional(readOnly = true)
    open fun findById(id: Int): FestivalTO? {
        val festival = festivalRepository?.findById(id.toString())?.orElse(null) ?: return null
        val modelMapper = ModelMapper()
        val to = modelMapper.map(festival, FestivalTO::class.java)

        val contentId: Int = festival.contentId
        val details = getFestivalDescription(contentId)

        to.festivalDetails = details

        return to
    }

    fun search(keyword: String?, pageable: Pageable?): Page<FestivalTO> {
        val festivalPage = festivalRepository!!.findByTitleContainingIgnoreCase(keyword, pageable)

        val modelMapper = ModelMapper()
        val page = festivalPage.map { festival: Festival? ->
            modelMapper.map(
                festival,
                FestivalTO::class.java
            )
        }
        return page
    }

    fun filterByMonth(year: Int?, month: Int?, pageable: Pageable?): Page<FestivalTO> {
        val currentDate = LocalDate.now()
        val safeYear = year ?: currentDate.year
        val safeMonth = month ?: currentDate.monthValue

        val yearMonth = YearMonth.of(safeYear, safeMonth)
        val firstDayOfMonth = yearMonth.atDay(1)
        val lastDayOfMonth = yearMonth.atEndOfMonth()

        val modelMapper = ModelMapper()
        val festivals = festivalRepository!!.findByMonth(firstDayOfMonth, lastDayOfMonth, pageable)
        val page = festivals.map { festival: Festival? ->
            modelMapper.map(
                festival,
                FestivalTO::class.java
            )
        }

        return page
    }

    fun filterByRegion(region: String?, pageable: Pageable?): Page<FestivalTO> {
        val festivals = festivalRepository!!.findByAddressContainingIgnoreCase(region, pageable)

        val modelMapper = ModelMapper()
        val page = festivals.map { festival: Festival? ->
            modelMapper.map(
                festival,
                FestivalTO::class.java
            )
        }
        return page
    }
}