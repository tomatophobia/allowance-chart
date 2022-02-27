package com.easywritten.allowancechart.adapter.in.page

import org.http4s.Uri
import scalatags.Text._
import scalatags.Text.all._

trait Base {
  def layout(pageTitle: String)(content: Frag): TypedTag[String] =
    html(
      head(
        commonHeaders(pageTitle)
      ),
      body(
      )
    )

  def commonHeaders(pageTitle: String): List[TypedTag[String]] =
    List(
      meta(charset := "utf-8"),
      tags2.title(pageTitle),
      link(
        rel := "stylesheet",
        href := "https://fonts.googleapis.com/css?family=Noto+Sans+KR:300,400,400i,700"
      ),
      link(
        rel := "stylesheet",
        href := adminLTE("plugins/fontawesome-free/css/all.min.css").toString()
      ),
      link(
        rel := "stylesheet",
        href := adminLTE("plugins/sweetalert2-theme-bootstrap-4/bootstrap-4.min.css")
      ),
      link(
        rel := "stylesheet",
        href := adminLTE("plugins/toastr/toastr.min.css")
      ),
      link(
        rel := "stylesheet",
        href := adminLTE("dist/css/adminlte.min.css")
      ),
      script(src := adminLTE("plugins/jquery/jquery.min.js")),
      script(src := adminLTE("plugins/bootstrap/js/bootstrap.bundle.min.js")),
      script(src := adminLTE("plugins/sweetalert2/sweetalert2.min.js")),
      script(src := adminLTE("plugins/toastr/toastr.min.js")),
      script(src := adminLTE("dist/js/adminlte.min.js")),
      script(src := plotly)
    )

  private val webjars: Uri = Uri.unsafeFromString("/webjars")
  def adminLTE(dir: String): Uri = (webjars / "AdminLTE" / "3.2.0").resolve(Uri.unsafeFromString(dir))
  val plotly: Uri = webjars / "plotly.js-dist-min" / "2.7.0" / "plotly.min.js"

  implicit val http4sUriAttrValue: scalatags.Text.AttrValue[Uri] = new scalatags.Text.GenericAttr[Uri]
}
