package me.michaelgagnon.pets.contest

import me.michaelgagnon.pets.web.actors.Pet

case class ContestResult(firstPlace: Pet, secondPlace: Pet, summary: Option[String])

trait Contest {
  def apply(pet1: Pet, pet2: Pet): ContestResult
}