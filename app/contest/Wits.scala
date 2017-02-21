package me.michaelgagnon.pets.contest

import  me.michaelgagnon.pets.web.actors.Pet

object Wits extends Contest {

  def getSummary(firstName: String, secondName: String): String =
    s"$firstName outsmarted $secondName"

  def apply(pet1: Pet, pet2: Pet): ContestResult =

    if (pet1.intelligence >= pet2.intelligence) {
      ContestResult(pet1, pet2, Some(getSummary(pet1.name, pet2.name)))
    } else {
      ContestResult(pet2, pet1, Some(getSummary(pet2.name, pet1.name)))
    }
}
