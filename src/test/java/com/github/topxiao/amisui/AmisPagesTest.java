package com.github.topxiao.amisui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AmisPagesTest {

    @Test
    void page_createsPageWithAllFields() {
        AmisProperties.Page page = AmisPages.page(
            "用户管理", "users", "get:/api/users", "fa-users");
        assertEquals("用户管理", page.getLabel());
        assertEquals("users", page.getUrl());
        assertEquals("get:/api/users", page.getSchemaApi());
        assertEquals("fa-users", page.getIcon());
        assertNotNull(page.getChildren());
        assertTrue(page.getChildren().isEmpty());
    }

    @Test
    void page_withChildren_createsPageWithChildren() {
        AmisProperties.Page child1 = AmisPages.page("子页1", "c1", "get:/api/c1", null);
        AmisProperties.Page child2 = AmisPages.page("子页2", "c2", "get:/api/c2", null);
        AmisProperties.Page parent = AmisPages.page(
            "父页", "parent", null, null, AmisPages.children(child1, child2));

        assertEquals("父页", parent.getLabel());
        assertEquals(2, parent.getChildren().size());
        assertEquals("子页1", parent.getChildren().get(0).getLabel());
    }

    @Test
    void group_createsPageWithLabelAndChildren() {
        AmisProperties.Page child = AmisPages.page("子页", "c", null, null);
        AmisProperties.Page group = AmisPages.group("分组", "fa-folder",
            AmisPages.children(child));

        assertEquals("分组", group.getLabel());
        assertEquals("fa-folder", group.getIcon());
        assertNull(group.getUrl());
        assertEquals(1, group.getChildren().size());
    }

    @Test
    void emptyChildren_returnsEmptyArray() {
        assertEquals(0, AmisPages.emptyChildren().length);
    }

    @Test
    void children_withNull_returnsEmptyArray() {
        assertEquals(0, AmisPages.children((AmisProperties.Page[]) null).length);
    }
}
