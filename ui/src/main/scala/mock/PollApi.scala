package mock

import apimodels.poll.{ Poll, PollOption, Open, Closed }

object PollApi {
  import java.time._
  def getAll = {
    Seq(
      Poll(
        "More points",
        "We want to know bla...bla...bla",
        "1",
        "John",
        LocalDate.of(2001, 1, 31),
        Open,
        Seq(PollOption("option1")),
        Some("1")),
      Poll(
        "Ability to filter contacts",
        "We want to know bla...bla...bla",
        "2",
        "Paul",
        LocalDate.of(2001, 1, 31),
        Open,
        Seq(PollOption("option1")),
        Some("2")),
      Poll(
        "Track by name or by id?",
        "We want to know bla...bla...bla",
        "3",
        "Bill",
        LocalDate.of(2001, 1, 31),
        Open,
        Seq(PollOption("option1")),
        Some("3")),
      Poll(

        "Free coins",
        "We want to know bla...bla...bla",
        "4",
        "Kent",
        LocalDate.of(2001, 1, 31),
        Closed,
        Seq(PollOption("option1")),
        Some("4")),
      Poll(
        "Zero return",
        "We want to know bla...bla...bla",
        "5",
        "Tom",
        LocalDate.of(2001, 1, 31),
        Closed,
        Seq(PollOption("option1")),
        Some("5")))
  }
}
