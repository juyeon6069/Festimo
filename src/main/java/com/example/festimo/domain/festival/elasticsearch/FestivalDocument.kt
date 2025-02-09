package com.example.festimo.domain.festival.elasticsearch

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.LocalDate

@Document(indexName = "festivals")
data class FestivalDocument(
    @Id var festival_id: Long = 0,
    var title: String = "",
    var address: String = "",
    @Field(type = FieldType.Date, pattern = ["yyyy/MM/dd"])
    var startDate: LocalDate? = null,
    @Field(type = FieldType.Date, pattern = ["yyyy/MM/dd"])
    var endDate: LocalDate? = null,
    var image: String? = null,
) {
    constructor() : this(0, "", "", null, null, null)
}
