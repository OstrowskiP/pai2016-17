package pl.lodz.p.edu.service

trait Routes extends RoutesWindows with RoutesUsers {
  def routes = routesWindows ~ routesUsers
}
