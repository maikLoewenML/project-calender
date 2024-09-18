package com.example.plugins

import CalenderApi
import com.example.entity.ProjectRequest
import com.example.entity.ProjectResponse
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        get("/") {
            call.respondText("Hello Maik!")
        }

        get("/test") {
            call.respondText("Hello Dennis")
        }

        get("/calenderapitest") {
            val api = CalenderApi()
            api.activate()
            call.respondText("Done")
        }

        /**
        post("/calenderapitest/createevent") {
        val api = CalenderApi()
        api.createEvent()
        call.respondText("Done")
        }
         */

        post("/") {
            call.respondText("Das hier ist ein post")
        }

        post("/calenderapi/createevent") {

            //hier ist es unnötig zuerst ein json zu erstellen und es dann wieder in die benötigten parameter zurück umzuwandeln

            val projectRequest = call.receive<ProjectRequest>()
            val api = CalenderApi()

            if (projectRequest.name != null) {
                //TODO EXCEPTIONS
                api.createProject(
                    projectRequest.name,
                    projectRequest.start,
                    projectRequest.deadline,
                    projectRequest.teilnehmer,
                    projectRequest.beschreibung,
                    projectRequest.ort
                )
                call.respond(ProjectResponse(true, "Das Projekt " + projectRequest.name + " wurde angelegt!"))
            } else
                call.respond(
                    ProjectResponse(
                        false,
                        "Es ist etwas schief gegangen. Überprüfe noch einmal deine Eingabe!"
                    )
                )
        }

    }
    routing {}
}
