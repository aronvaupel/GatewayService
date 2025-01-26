package com.ecommercedemo.gateway.filter

import com.ecommercedemo.gateway.config.web.RequestCachingFilter
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.util.ContentCachingRequestWrapper
import kotlin.test.assertEquals

class RequestCachingFilterTest {

    private lateinit var filter: RequestCachingFilter
    private lateinit var chain: FilterChain
    private lateinit var response: MockHttpServletResponse

    @BeforeEach
    fun setUp() {
        filter = RequestCachingFilter()
        chain = mock(FilterChain::class.java)
        response = MockHttpServletResponse()
    }

    @Test
    fun `should skip request caching for auth endpoint`() {
        val request = MockHttpServletRequest().apply {
            requestURI = "/auth/login"
        }

        filter.doFilterInternal(request, response, chain)

        verify(chain).doFilter(request, response)
    }

    @Test
    fun `should handle already wrapped requests gracefully`() {
        val request = ContentCachingRequestWrapper(MockHttpServletRequest().apply {
            requestURI = "/api/data"
            method = "POST"
            contentType = "application/json"
            setContent("""{"key":"value"}""".toByteArray())
        })

        filter.doFilterInternal(request, response, chain)

        assertEquals(
            """{"key":"value"}""",
            String(request.contentAsByteArray),
            "Wrapped request body should not be modified"
        )
        verify(chain).doFilter(eq(request), eq(response))
    }
}
