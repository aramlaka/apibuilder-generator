package go.models

import com.bryzek.apidoc.spec.v0.models.{Application, Import, Organization}
import org.scalatest.{FunSpec, Matchers}

class ImportBuilderSpec extends FunSpec with Matchers {

  def buildImports(
    namespace: String,
    enums: Seq[String] = Nil,
    unions: Seq[String] = Nil,
    models: Seq[String] = Nil
  ): Seq[Import] = {
    Seq(
      Import(
        uri = "http://apidoc.me/test/app/0.0.1/service.json",
        namespace = namespace,
        organization = Organization("test"),
        application = Application("app"),
        version = "0.0.1",
        enums = enums,
        unions = unions,
        models = models
      )
    )
  }

  it("empty if no imports") {
    val builder = ImportBuilder(Nil)
    builder.generate() should be("")
  }

  it("prefixes with package name") {
    val builder = ImportBuilder(Nil)

    builder.ensureImport("json")
    builder.ensureImport("encoding/json")
    builder.ensureImport("other/json")

    builder.generate() should be("""
import (
	encodingJson "encoding/json"
	"json"
	otherJson "other/json"
)
""".trim)
  }

  it("idempotent") {
    val builder = ImportBuilder(Nil)
    builder.ensureImport("io")
    builder.ensureImport("io")
    builder.generate() should be("""
import (
	"io"
)
""".trim)
  }

  it("alphabetizes") {
    val builder = ImportBuilder(Nil)
    builder.ensureImport("io")
    builder.ensureImport("fmt")
    builder.ensureImport("net/http")
    builder.generate() should be("""
import (
	"fmt"
	"io"
	"net/http"
)
""".trim)
  }

  it("aliases if needed") {
    val builder = ImportBuilder(Nil)
    builder.ensureImport("io.flow.common.v0.models")
    builder.generate() should be("""
import (
	common "github.com/flowcommerce/apidoc/common"
)
""".trim)
  }

  it("aliases duplicate imports") {
    val builder = ImportBuilder(Nil)
    builder.ensureImport("common")
    builder.ensureImport("net/common")
    builder.generate() should be("""
import (
	"common"
	netCommon "net/common"
)
""".trim)
  }

  it("aliases duplicate imports with org name when available") {
    val builder = ImportBuilder(Nil)
    builder.ensureImport("common")
    builder.ensureImport("io.flow.common.v0.models")
    builder.generate() should be("""
import (
	"common"
	flowcommerceCommon "github.com/flowcommerce/apidoc/common"
)
""".trim)
  }

  it("resolves multi package imports") {
    val builder = ImportBuilder(Nil)

    builder.ensureImport("io.flow.carrier.account.v0.unions.expandable_carrier_account")

    println("")
    println(builder.generate())
    println("")

    builder.generate() should be("""
import (
	carrierAccount "github.com/flowcommerce/apidoc/carrier/account"
)
""".trim)
  }
  
}
