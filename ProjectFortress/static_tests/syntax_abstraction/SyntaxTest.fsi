(*******************************************************************************
    Copyright 2008 Sun Microsystems, Inc.,
    4150 Network Circle, Santa Clara, California 95054, U.S.A.
    All rights reserved.

    U.S. Government Rights - Commercial software.
    Government users are subject to the Sun Microsystems, Inc. standard
    license agreement and applicable provisions of the FAR and its supplements.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.

    Sun, Sun Microsystems, the Sun logo and Java are trademarks or registered
    trademarks of Sun Microsystems, Inc. in the U.S. and other countries.
 ******************************************************************************)

api SyntaxTest

  import FortressAst.{...}
  import FortressSyntax.{...}
  import List.{...}

  grammar Helloworld extends { Literal }
      LiteralExpr |Expr:=
        Fortress is very? a:cool# b:, indeed Verys* c:cool
        do 
          ids: List[\Id\] = emptyList[\Id\](1)
          ids1: List[\Id\] = ids.addRight(Id("FortressLibrary"))
          apiName:APIName = APIName(ids1)
          printName:Id = Id("print")
          name: QualifiedIdName = QualifiedIdName(Just[\APIName\](apiName), printName)
          exprs: List[\Expr\] = emptyList[\Expr\](2)
          exprs1: List[\Expr\] = exprs.addRight(FnRef( <| name |> , emptyList[\StaticArg\]()))
          es:List[\Expr\] = if v <- very then
                                       exprs1.addRight(StringLiteralExpr(Fortress " " is " " v " " a b " " indeed " " Verys " " c))
                                     else
                                       exprs1.addRight(StringLiteralExpr(Fortress " " is " " a b " " indeed " " Verys " " c " "))
                                     end
          LooseJuxt(es)
        end

      Verys :StringLiteralExpr:= SPACE a:very do StringLiteralExpr(a) end
  end
 
end