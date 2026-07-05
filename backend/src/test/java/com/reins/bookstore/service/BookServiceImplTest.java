package com.reins.bookstore.service;

import com.reins.bookstore.dto.response.BookDTO;
import com.reins.bookstore.entity.Book;
import com.reins.bookstore.repository.BookRepository;
import com.reins.bookstore.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * BookServiceImpl 的单元测试
 *
 * 什么是 Mock 测试？
 *   Mock 即"模拟对象"——用假的对象替换真实依赖，隔离测试目标。
 *   比如这里的 BookService 依赖 BookRepository（数据库），
 *   我们不希望测试 BookService 时真的去连数据库，
 *   而是用 Mock 的 BookRepository 模拟返回数据。
 *
 * 为什么用 Mock？
 *   1. 速度快：不需要启动数据库
 *   2. 隔离性好：只测 BookService 的逻辑，不测 BookRepository
 *   3. 可控性高：可以模拟各种极端情况（查不到、抛异常等）
 */
@ExtendWith(MockitoExtension.class)  // 让 Mockito 框架自动初始化 @Mock 和 @InjectMocks
class BookServiceImplTest {

    /**
     * @Mock 注解
     * 创建一个"假的" BookRepository，不连真正的数据库
     * 所有对 bookRepository 的调用都不会执行真实逻辑
     * 需要通过 when(...).thenReturn(...) 来指定"当调用某个方法时，返回什么"
     */
    @Mock
    private BookRepository bookRepository;

    /**
     * @InjectMocks 注解
     * 创建一个"真的" BookServiceImpl，并把上面 @Mock 的对象自动注入进去
     * 效果等价于：bookService = new BookServiceImpl(bookRepository)
     * 这样 bookService 内部用的就是 mock 的 bookRepository，不会操作真实数据库
     */
    @InjectMocks
    private BookServiceImpl bookService;

    /**
     * 测试用的"假数据"——模拟从数据库查出来的实体对象
     * 这些对象和真实数据库无关，只是内存中的 Java 对象
     */
    private Book book1;
    private Book book2;

    /**
     * @BeforeEach 注解
     * 在每个 @Test 方法执行前都会运行这个方法
     * 用来初始化测试数据，避免每个测试方法重复写
     */
    @BeforeEach
    void setUp() {
        // 模拟第一本书（Spring in Action）
        book1 = new Book();
        book1.setId(1L);
        book1.setTitle("Spring in Action");
        book1.setAuthor("Craig Walls");
        book1.setCover("spring-cover.jpg");
        book1.setPrice(59.99);        // Book 实体里是 Double 类型
        book1.setDesc("A comprehensive guide to Spring");
        book1.setInventory(50);
        book1.setPublisher("Manning");
        book1.setIsbn("978-1617294945");
        book1.setSales(100);
        book1.setRecommended(true);

        // 模拟第二本书（Clean Code）
        book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Clean Code");
        book2.setAuthor("Robert C. Martin");
        book2.setCover("cleancode-cover.jpg");
        book2.setPrice(49.99);
        book2.setDesc("A handbook of agile software craftsmanship");
        book2.setInventory(30);
        book2.setPublisher("Prentice Hall");
        book2.setIsbn("978-0132350884");
        book2.setSales(80);
        book2.setRecommended(false);
    }

    // ==================== findAll() 测试 ====================

    @Test
    void findAll_shouldReturnAllBooksAsDTOs() {
        // ── 准备（Given）──
        // 关键语法：when( mock对象.方法名(参数) ).thenReturn( 返回值 )
        // 意思是：当调用 bookRepository.findAll() 时，不要真的查数据库，
        //         而是直接返回 book1, book2 这个列表
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        // ── 执行（When）──
        // 调用真正的 BookServiceImpl.findAll()
        // 但 bookService 内部用的是 mock 的 bookRepository
        // 所以不会执行 SQL，而是返回上面指定的假数据
        List<BookDTO> result = bookService.findAll();

        // ── 验证（Then）──
        assertNotNull(result);                    // 结果不应为 null
        assertEquals(2, result.size());            // 应该有 2 本书
        assertEquals("Spring in Action", result.get(0).getTitle());  // 第一本是 Spring
        assertEquals("Clean Code", result.get(1).getTitle());        // 第二本是 Clean Code
        // 验证 DTO 转换：price 从 Double(59.99) 变成了 Integer(59)
        assertEquals(Integer.valueOf(59), result.get(0).getPrice()); // 59.99.intValue() = 59
        assertEquals(50, result.get(0).getInventory());              // 库存 50
        assertEquals("978-1617294945", result.get(0).getIsbn());     // ISBN

        // verify() 用来验证"某个方法是否被调用了指定次数"
        // 这里验证：bookRepository.findAll() 确实被调用了 1 次
        // 如果 bookService.findAll() 内部没有调用 findAll()，测试会失败
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoBooks() {
        // 模拟数据库里没有数据的情况
        when(bookRepository.findAll()).thenReturn(Collections.emptyList());

        List<BookDTO> result = bookService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());  // 列表应为空
    }

    // ==================== findById() 测试 ====================

    @Test
    void findById_shouldReturnBookDTOWhenFound() {
        // 模拟：调用 findById(1L) 时返回 book1（JPA 的 findById 返回 Optional）
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));

        Optional<BookDTO> result = bookService.findById(1L);

        assertTrue(result.isPresent());                        // Optional 应该有值
        assertEquals("Spring in Action", result.get().getTitle());
        assertEquals("Craig Walls", result.get().getAuthor());
        assertEquals(Boolean.TRUE, result.get().getRecommended());  // 推荐标识转换正确
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        // 模拟：查一个不存在的 ID，返回 Optional.empty()
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<BookDTO> result = bookService.findById(999L);

        assertFalse(result.isPresent());  // 预期的"找不到"
    }

    // ==================== searchByTitle() 测试 ====================

    @Test
    void searchByTitle_shouldReturnMatchingBooks() {
        // 模拟：按书名搜索 "spring" 只匹配到 book1（Spring in Action）
        when(bookRepository.findByTitleContainingIgnoreCase("spring")).thenReturn(Arrays.asList(book1));

        List<BookDTO> result = bookService.searchByTitle("spring");

        assertNotNull(result);
        assertEquals(1, result.size());  // 只有一本匹配
        assertEquals("Spring in Action", result.get(0).getTitle());
    }

    @Test
    void searchByTitle_shouldReturnAllWhenTitleIsNull() {
        // 边界情况：搜索条件为 null——应返回全部书籍
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        List<BookDTO> result = bookService.searchByTitle(null);

        assertEquals(2, result.size());
        // 验证：title 为 null 时内部调用了 findAll() 而不是 findByTitleContaining()
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void searchByTitle_shouldReturnAllWhenTitleIsEmpty() {
        // 边界情况：搜索条件为空格——也应该返回全部
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        List<BookDTO> result = bookService.searchByTitle("   ");

        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void searchByTitle_shouldReturnEmptyWhenNoMatch() {
        // 模拟：搜不存在的书名，返回空列表
        when(bookRepository.findByTitleContainingIgnoreCase("nonexistent")).thenReturn(Collections.emptyList());

        List<BookDTO> result = bookService.searchByTitle("nonexistent");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== saveBook() 测试 ====================

    @Test
    void saveBook_shouldSaveAndReturnDTO() {
        // ── 准备 ──
        // 模拟前端传来的 DTO
        BookDTO dto = new BookDTO();
        dto.setTitle("New Book");
        dto.setAuthor("New Author");
        dto.setCover("new-cover.jpg");
        dto.setPrice(3999);            // DTO 用 Integer（单位：分）
        dto.setDescription("A brand new book");
        dto.setInventory(20);
        dto.setPublisher("New Publisher");
        dto.setIsbn("978-0000000001");

        // 模拟 bookRepository.save() 返回的实体
        // any(Book.class) 表示"接受任意 Book 类型的参数"
        // 因为 save() 的参数是 bookService 内部 new 出来的，我们没法精确匹配
        Book savedBook = new Book();
        savedBook.setId(3L);            // 数据库自动生成 ID
        savedBook.setTitle("New Book");
        savedBook.setAuthor("New Author");
        savedBook.setCover("new-cover.jpg");
        savedBook.setPrice(39.99);      // Entity 里用 Double
        savedBook.setDesc("A brand new book");
        savedBook.setInventory(20);
        savedBook.setPublisher("New Publisher");
        savedBook.setIsbn("978-0000000001");
        savedBook.setSales(0);          // 新增书籍销量为 0

        // 设置 mock：无论 save() 收到什么 Book，都返回 savedBook
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        // ── 执行 ──
        BookDTO result = bookService.saveBook(dto);

        // ── 验证 ──
        assertNotNull(result);
        assertEquals("New Book", result.getTitle());
        // 验证 DTO 转换：price 从 Double(39.99) → Integer(39)
        assertEquals(Integer.valueOf(39), result.getPrice()); // 39.99.intValue() = 39
        assertEquals(20, result.getInventory());
        // 验证 bookRepository.save() 确实被调用了一次
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void saveBook_shouldSetDefaultInventoryWhenNull() {
        // 测试：当 DTO 没有传库存时，应使用默认库存 100
        BookDTO dto = new BookDTO();
        dto.setTitle("Book Without Inventory");
        dto.setAuthor("Author");
        dto.setPrice(1999);
        // 注意：没有设置 dto.setInventory()

        Book savedBook = new Book();
        savedBook.setId(4L);
        savedBook.setTitle("Book Without Inventory");
        savedBook.setInventory(100);   // BookServiceImpl 中默认值为 100

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        BookDTO result = bookService.saveBook(dto);

        assertNotNull(result);
        assertEquals(100, result.getInventory());  // 确认默认库存生效
    }

    @Test
    void saveBook_shouldHandleNullPrice() {
        // 测试：价格为空时的边界情况
        BookDTO dto = new BookDTO();
        dto.setTitle("Free Book");
        dto.setAuthor("Author");
        dto.setPrice(null);

        Book savedBook = new Book();
        savedBook.setId(5L);
        savedBook.setTitle("Free Book");
        savedBook.setPrice(null);

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        BookDTO result = bookService.saveBook(dto);

        assertNotNull(result);
        assertNull(result.getPrice());  // 价格为 null 应正常处理
    }

    // ==================== updateBook() 测试 ====================

    @Test
    void updateBook_shouldUpdateExistingBook() {
        // 测试更新书籍：先查再改
        BookDTO dto = new BookDTO();
        dto.setTitle("Updated Title");
        dto.setPrice(2999);      // 新价格 29.99 元
        dto.setInventory(60);

        // 模拟更新后的结果
        Book updatedBook = new Book();
        updatedBook.setId(1L);
        updatedBook.setTitle("Updated Title");
        updatedBook.setAuthor("Craig Walls");     // 未修改字段保留原值
        updatedBook.setCover("spring-cover.jpg");
        updatedBook.setPrice(29.99);
        updatedBook.setDesc("A comprehensive guide to Spring");
        updatedBook.setInventory(60);
        updatedBook.setPublisher("Manning");
        updatedBook.setIsbn("978-1617294945");
        updatedBook.setSales(100);

        // 两步模拟：先查找到，再保存
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));   // findById 找到
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);  // save 返回更新后

        BookDTO result = bookService.updateBook(1L, dto);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals(Integer.valueOf(29), result.getPrice()); // 29.99.intValue() = 29
        assertEquals(60, result.getInventory());
        // 验证两个操作各执行了一次
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void updateBook_shouldReturnNullWhenBookNotFound() {
        // 测试：更新不存在的书籍
        BookDTO dto = new BookDTO();
        dto.setTitle("Won't work");

        when(bookRepository.findById(999L)).thenReturn(Optional.empty());  // 找不到

        BookDTO result = bookService.updateBook(999L, dto);

        assertNull(result);  // 应返回 null
        // never() 验证：save() 从未被调用——因为没找到就不应该执行保存
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_shouldOnlyUpdateNonNullFields() {
        // 测试：DTO 中只传了 title，其他字段为 null——应只更新 title，其他保留
        BookDTO dto = new BookDTO();
        dto.setTitle("New Title Only");
        // 注意：author, price, inventory 等都为 null

        Book updatedBook = new Book();
        updatedBook.setId(1L);
        updatedBook.setTitle("New Title Only");     // 只改了标题
        updatedBook.setAuthor("Craig Walls");       // 作者保持不变
        updatedBook.setPrice(59.99);                // 价格保持不变

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

        BookDTO result = bookService.updateBook(1L, dto);

        assertNotNull(result);
        assertEquals("New Title Only", result.getTitle());  // 标题更新了
        assertEquals("Craig Walls", result.getAuthor());    // 作者没变
        assertEquals(Integer.valueOf(59), result.getPrice()); // 价格没变
    }

    // ==================== deleteBook() 测试 ====================

    @Test
    void deleteBook_shouldDeleteWhenExists() {
        // 模拟：书籍存在，删除成功
        when(bookRepository.existsById(1L)).thenReturn(true);
        // doNothing() 用于 void 方法——deleteById 没有返回值
        doNothing().when(bookRepository).deleteById(1L);

        Map<String, Object> result = bookService.deleteBook(1L);

        assertNotNull(result);
        assertEquals("删除成功", result.get("message"));
        assertNull(result.get("error"));           // 没有错误信息
        verify(bookRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteBook_shouldReturnErrorWhenNotExists() {
        // 模拟：书籍不存在
        when(bookRepository.existsById(999L)).thenReturn(false);

        Map<String, Object> result = bookService.deleteBook(999L);

        assertNotNull(result);
        assertEquals("书籍不存在", result.get("error"));
        assertNull(result.get("message"));
        // never()：不存在，所以 deleteById 不应该被调用
        verify(bookRepository, never()).deleteById(any());
    }

    // ==================== DTO 转换测试 ====================

    @Test
    void toDTO_shouldHandleNullPrice() {
        // 测试：Entity 中 price 为 null 时，DTO 的 price 也应为 null
        // 防止 NullPointerException
        book1.setPrice(null);
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1));

        List<BookDTO> result = bookService.findAll();

        assertNull(result.get(0).getPrice());  // null → null，不崩溃
    }
}
