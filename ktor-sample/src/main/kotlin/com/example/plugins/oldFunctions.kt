package com.example.plugins

import CalenderApi
import com.example.entity.ProjectRequest
import com.example.entity.ProjectResponse
import com.google.api.client.util.DateTime
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureRoutingOldFunctions() {

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
            /**
             * hier werden erst mal aus der Konsole die relevanten Projektinformationen gesammelt
             * TODO: Umwandlung aus Stringformatierung in DateTime
             */
            val scanner = Scanner(System.`in`)
            val projektstarttest = DateTime("2022-11-28T09:00:00-07:00")
            val projektdeadlinetest = DateTime("2022-11-28T09:00:00-07:00")
            println("Meetingdauer: ")
            var meetingdauer = (scanner.nextLine()).toDouble()
            println("\nTeilnehmer: ")
            var teilnehmer = scanner.nextLine()
            println("\nProjektbeschreibung: ")
            var projektbeschreibung = scanner.nextLine()
            println("\nBesprechungsort: ")
            var besprechungsort = scanner.nextLine()
            scanner.close()
            val api = CalenderApi()
            if (projektbeschreibung != null && besprechungsort != null && meetingdauer != null && teilnehmer != null) {
                /**
                 * hier war die Heuristik noch nicht eingebaut und die Parameter haben sich geändert.
                 * Deswegen ist die createEvent-Funktion so nicht mehr ausführbar
                 */
                //api.createEvent(projektstarttest, projektdeadlinetest, meetingdauer, teilnehmer, projektbeschreibung, besprechungsort)
                //call.respond(LoginResponse(true, "Es wurde ein Eintrag mit folgenden Parametern erstellt: \n " +
                //        "Projektstart: $projektstarttest\n" +
                //        "Projektdeadline: $projektdeadlinetest\n" +
                //        "Meetingdauer: $meetingdauer\n" +
                //        "Teilnehmer: $teilnehmer\n" +
                //        "Projektbeschreibung: $projektbeschreibung\n" +
                //        "Besprechungsort: $besprechungsort\n"))
            }
            //else
                //call.respond(LoginResponse(false, "Es ist etwas schief gegangen. Überprüfe noch einmal deine Eingabe!"))
        }

    }
    routing {}
}
