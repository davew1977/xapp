package net.sf.xapp.net.server.idgen;

import ngpoker.infrastructure.types.EntityType;
import net.sf.xapp.net.server.clustering.NodeInfoImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 */
public class FileIdSeqGeneratorTest {

    @Test
    public void testNextId() throws Exception {
        FileIdSeqGenerator f =  new FileIdSeqGenerator(new NodeInfoImpl(1, ""));

        assertEquals("1_c_0", f.nextId(EntityType.cashgame));
        assertEquals("1_c_1", f.nextId(EntityType.cashgame));
        assertEquals("1_c_2", f.nextId(EntityType.cashgame));
        assertEquals("1_c_3", f.nextId(EntityType.cashgame));
        assertEquals("1_s_0", f.nextId(EntityType.sng));
        assertEquals("1_p_0", f.nextId(EntityType.player));
        assertEquals("1_t_0", f.nextId(EntityType.tournament));
        assertEquals("1_p_1", f.nextId(EntityType.player));
        assertEquals("1_c_4", f.nextId(EntityType.cashgame));
        assertEquals(5, f.peek(EntityType.cashgame));
    }
}
