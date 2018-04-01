package mock

import apimodels.poll.{Poll, PollOption, Open, Closed}

object PollApi {
  import java.time._
  def getAll = {
    Seq(
      Poll(Some("1"),
           "More points",
           "We want to know bla...bla...bla",
           "1",
           Some("John"),
           LocalDate.of(2001, 1, 31),
           Open,
           Seq(PollOption("option1"))),
      Poll(
        Some("2"),
        "Ability to filter contacts",
        "We want to know bla...bla...bla",
        "2",
        Some("Paul"),
        LocalDate.of(2001, 1, 31),
        Open,
        Seq(PollOption("option1"))
      ),
      Poll(
        Some("3"),
        "Track by name or by id?",
        "We want to know bla...bla...bla",
        "3",
        Some("Bill"),
        LocalDate.of(2001, 1, 31),
        Open,
        Seq(PollOption("option1"))
      ),
      Poll(
        Some("4"),
        "Free coins",
        "We want to know bla...bla...bla",
        "4",
        Some("Kent"),
        LocalDate.of(2001, 1, 31),
        Closed,
        Seq(PollOption("option1"))
      ),
      Poll(Some("5"),
           "Zero return",
           "We want to know bla...bla...bla",
           "5",
           Some("Tom"),
           LocalDate.of(2001, 1, 31),
           Closed,
           Seq(PollOption("option1")))
    )
  }
}
