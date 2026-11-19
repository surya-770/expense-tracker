package com.expensetracker.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {
    @Autowired MockMvc mvc;

    @Test void registrationProtectsProfileAndSeparatesUserRecords() throws Exception {
        String suffix=java.util.UUID.randomUUID().toString().substring(0,8);
        MockHttpSession first=register("first-"+suffix+"@example.com");
        mvc.perform(get("/api/profile").session(first)).andExpect(status().isOk()).andExpect(jsonPath("$.email").value("first-"+suffix+"@example.com"));
        MvcResult category=mvc.perform(post("/api/categories").session(first).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Food\",\"type\":\"EXPENSE\",\"icon\":\"food\",\"color\":\"#4477AA\"}"))
                .andExpect(status().isCreated()).andReturn();
        long categoryId=Long.parseLong(com.jayway.jsonpath.JsonPath.read(category.getResponse().getContentAsString(),"$.id").toString());
        MvcResult transaction=mvc.perform(post("/api/transactions").session(first).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"EXPENSE\",\"amount\":\"12.50\",\"date\":\"2026-11-05\",\"categoryId\":"+categoryId+",\"paymentMethod\":\"UPI\",\"description\":\"Lunch\"}"))
                .andExpect(status().isCreated()).andReturn();
        long transactionId=Long.parseLong(com.jayway.jsonpath.JsonPath.read(transaction.getResponse().getContentAsString(),"$.id").toString());
        MockHttpSession second=register("second-"+suffix+"@example.com");
        mvc.perform(get("/api/transactions/"+transactionId).session(second)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Transaction not found"));
    }

    private MockHttpSession register(String email) throws Exception {
        MvcResult result=mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Test User\",\"email\":\""+email+"\",\"password\":\"Strong!Passphrase2\",\"confirmPassword\":\"Strong!Passphrase2\",\"currency\":\"USD\"}"))
                .andExpect(status().isCreated()).andReturn();
        return (MockHttpSession)result.getRequest().getSession(false);
    }
}
