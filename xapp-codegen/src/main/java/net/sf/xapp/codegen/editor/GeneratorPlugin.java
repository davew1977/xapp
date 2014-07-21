/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.editor;

import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.codegen.model.Model;

import java.util.List;

public interface GeneratorPlugin
{
    List<CodeFile> generate(Model model, Node node);

}