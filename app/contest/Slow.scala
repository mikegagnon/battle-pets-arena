package me.michaelgagnon.pets.contest

import java.lang.Thread

import  me.michaelgagnon.pets.web.actors.Pet

object Slow extends Contest {

  def apply(pet1: Pet, pet2: Pet): ContestResult = {

    Thread.sleep(5000)

    ContestResult(pet1, pet2, None)

  }

}
