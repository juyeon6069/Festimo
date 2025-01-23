package com.example.festimo.festival;

import com.example.festimo.domain.festival.dto.FestivalTO;
import com.example.festimo.domain.festival.service.FestivalService;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
public class PaginationPerformanceTest {
    private FestivalService festivalService;

    public PaginationPerformanceTest() {
        // Mock 객체 생성
        this.festivalService = Mockito.mock(FestivalService.class);

        // Mock 데이터 설정
        List<FestivalTO> mockData = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            FestivalTO festivalTO = new FestivalTO();
            festivalTO.setTitle("축제 " + i);
            mockData.add(festivalTO);
        }

        // Mock 객체의 메서드 호출 결과 정의
        Page<FestivalTO> mockPage = new PageImpl<>(mockData, PageRequest.of(0, 10), mockData.size());
        Mockito.when(festivalService.findPaginated(PageRequest.of(0, 10))).thenReturn(mockPage);
        Mockito.when(festivalService.findPaginatedWithCache(PageRequest.of(0, 10))).thenReturn(mockPage);
    }

    public static void main(String[] args) {
        PaginationPerformanceTest test = new PaginationPerformanceTest();

        // Redis 사용 전
        long startWithoutRedis = System.nanoTime();
        test.fetchDataFromDatabase();
        long endWithoutRedis = System.nanoTime();
        System.out.println("Without Redis: " +
                TimeUnit.NANOSECONDS.toMillis(endWithoutRedis - startWithoutRedis) + " ms");

        // Redis 사용 후
        long startWithRedis = System.nanoTime();
        test.fetchDataFromRedis();
        long endWithRedis = System.nanoTime();
        System.out.println("With Redis: " +
                TimeUnit.NANOSECONDS.toMillis(endWithRedis - startWithRedis) + " ms");
    }

    public void fetchDataFromDatabase() {
        Page<FestivalTO> page = festivalService.findPaginated(PageRequest.of(0, 10));
    }

    public void fetchDataFromRedis() {
        Page<FestivalTO> page = festivalService.findPaginatedWithCache(PageRequest.of(0, 10));
    }
}
