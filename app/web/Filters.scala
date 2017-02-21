package me.michaelgagnon.pets.web

import javax.inject.Inject

import me.michaelgagnon.pets.web.filters.AuthFilter
import play.api.http.DefaultHttpFilters

class Filters @Inject() (auth: AuthFilter) extends DefaultHttpFilters(auth)
