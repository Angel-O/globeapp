package mock

import apimodels.poll.{ Poll, PollOption }

object PollApi {
  import java.time._
  def getAll = {
    Seq(
      Poll(
        Some("1"),
        "More points",
        "We want to know bla...bla...bla",
        "1",
        "John",
        LocalDate.of(2001, 1, 31),
        "open",
        Seq(PollOption("option1", Seq("me")))),
      Poll(
        Some("2"),
        "Ability to filter contacts",
        "We want to know bla...bla...bla",
        "2",
        "Paul",
        LocalDate.of(2001, 1, 31),
        "open",
        Seq(PollOption("option1", Seq("me")))),
      Poll(
        Some("3"),
        "Track by name or by id?",
        "We want to know bla...bla...bla",
        "3",
        "Bill",
        LocalDate.of(2001, 1, 31),
        "open",
        Seq(PollOption("option1", Seq("me", "bla")))),
      Poll(
        Some("4"),
        "Free coins",
        "We want to know bla...bla...bla",
        "4",
        "Kent",
        LocalDate.of(2001, 1, 31),
        "closed",
        Seq(PollOption("option1", Seq("me", "they")))),
      Poll(
        Some("5"),
        "Zero return",
        "We want to know bla...bla...bla",
        "5",
        "tom",
        LocalDate.of(2001, 1, 31),
        "closed",
        Seq(PollOption("option1", Seq("me", "you")))))
  }
}