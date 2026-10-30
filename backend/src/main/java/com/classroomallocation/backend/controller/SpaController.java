package com.classroomallocation.backend.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({
            "/",
            "/login",
            "/forgot-password",
            "/reset-password",
            "/dashboard",
            "/change-password",
            "/users",
            "/classrooms",
            "/teachers",
            "/subjects",
            "/sections",
            "/time-slots",
            "/requirements",
            "/allocations",
            "/allocation-drafts",
            "/student-section-assignments",
            "/my-timetable"
    })
    public String application(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return "forward:/index.html";
    }
}