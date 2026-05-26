package com.quotify.core.data.mapper

import com.quotify.core.data.localDatabase.QuoteEntity
import com.quotify.core.data.model.QuoteAPIResponse
import com.quotify.core.domain.model.Quote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class QuoteMapperTest {
    @Test
    fun `QuoteAPIResponse is correctly mapped to QuoteEntity`() {
        val quoteAPIResponse =
            QuoteAPIResponse(
                id = 1,
                quote = "Miles to go before I sleep!",
                author = "Robert Frost",
            )

        val expectedEntity =
            QuoteEntity(
                id = "1",
                author = "Robert Frost",
                quote = "Miles to go before I sleep!",
            )

        assertEquals(
            "toEntity should map all fields correctly from QuoteAPIResponse",
            expectedEntity,
            quoteAPIResponse.toEntity(),
        )
    }

    @Test
    fun `QuoteEntity is correctly mapped to Quote`() {
        val quoteEntity =
            QuoteEntity(
                id = "1",
                author = "Robert Frost",
                quote = "Miles to go before I sleep!",
                favorite = true,
            )

        val expectedDomain =
            Quote(
                id = "1",
                content = "Miles to go before I sleep!",
                author = "Robert Frost",
                favorite = true,
            )

        assertEquals(
            "toDomain should map all fields correctly including favorite = true",
            expectedDomain,
            quoteEntity.toDomain(),
        )
    }

    @Test
    fun `QuoteEntity with favorite false is correctly mapped to Quote`() {
        val quoteEntity =
            QuoteEntity(
                id = "2",
                author = "Unknown",
                quote = "Hello World",
                favorite = false,
            )

        val expectedDomain =
            Quote(
                id = "2",
                content = "Hello World",
                author = "Unknown",
                favorite = false,
            )

        assertEquals("toDomain should map favorite = false correctly", expectedDomain, quoteEntity.toDomain())
    }

    @Test
    fun `toDomain does not match unrelated Quote object`() {
        val quoteEntity =
            QuoteEntity(
                id = "2",
                author = "Unknown",
                quote = "Hello World",
            )

        val expectedDomain =
            Quote(
                id = "1",
                content = "Hello World",
                author = "Unknown",
                favorite = true,
            )

        assertNotEquals(
            "toDomain should produce a domain model that accurately reflects the entity",
            expectedDomain,
            quoteEntity.toDomain(),
        )
    }

    @Test
    fun `toEntity converts numeric id to string`() {
        val response =
            QuoteAPIResponse(
                id = 12345,
                quote = "Be yourself",
                author = "Oscar Wilde",
            )

        val entity = response.toEntity()

        assertEquals("numeric id should be converted to string id in QuoteEntity", "12345", entity.id)
    }

    @Test
    fun `toEntity defaults favorite to false`() {
        val response = QuoteAPIResponse(id = 1, quote = "q", author = "a")

        val entity = response.toEntity()

        assertEquals("toEntity should default favorite to false for new mappings", false, entity.favorite)
    }
}
