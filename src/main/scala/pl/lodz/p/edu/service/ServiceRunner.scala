package pl.lodz.p.edu.service


object ServiceRunner {
  def main(args: Array[String]) {
    val httpService = new HttpService
    httpService.run
  }
}
