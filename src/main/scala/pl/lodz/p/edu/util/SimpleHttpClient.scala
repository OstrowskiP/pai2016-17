package pl.lodz.p.edu.util

import org.apache.http.HttpResponse
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient

/**
  * Created by ostrowsp on 2017-01-12.
  */
class SimpleHttpClient(host: String, port: Int) {

  def post(json: String, path: String): String = {
    val post = new HttpPost(s"https://$host:$port/$path")
    post.setHeader("Content-type", "application/json")
    post.setEntity(new StringEntity(json))
    val response = (new DefaultHttpClient).execute(post)
    response.getStatusLine.toString
  }

  def get(path: String): HttpResponse = {
    val get = new HttpGet(s"http://$host:$port/$path")
    (new DefaultHttpClient).execute(get)
  }
}
