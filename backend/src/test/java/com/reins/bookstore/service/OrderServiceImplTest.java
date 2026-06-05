package com.reins.bookstore.service;

import com.reins.bookstore.dto.response.OrderDTO;
import com.reins.bookstore.dto.response.OrderItemDTO;
import com.reins.bookstore.entity.Book;
import com.reins.bookstore.entity.Cart;
import com.reins.bookstore.entity.Order;
import com.reins.bookstore.entity.OrderItem;
import com.reins.bookstore.repository.BookRepository;
import com.reins.bookstore.repository.CartRepository;
import com.reins.bookstore.repository.OrderItemRepository;
import com.reins.bookstore.repository.OrderRepository;
import com.reins.bookstore.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Book testBook;
    private Cart cartItem;
    private Order testOrder;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Author");
        testBook.setCover("cover.jpg");
        testBook.setPrice(29.99);
        testBook.setInventory(50);
        testBook.setSales(10);

        cartItem = new Cart();
        cartItem.setId(1L);
        cartItem.setBookId(1L);
        cartItem.setUserId(1L);
        cartItem.setNumber(2);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUserId(1L);
        testOrder.setReceiver("John Doe");
        testOrder.setAddress("123 Main St");
        testOrder.setTel("1234567890");
        testOrder.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));

        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setOrderId(1L);
        testOrderItem.setBookId(1L);
        testOrderItem.setBookTitle("Test Book");
        testOrderItem.setBookCover("cover.jpg");
        testOrderItem.setNumber(2);
        testOrderItem.setUnitPrice(2999); // 29.99 * 100
    }

    // ========== FIND ORDERS BY USER ID ==========

    @Test
    void findOrdersByUserId_shouldReturnOrders() {
        when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        List<OrderDTO> result = orderService.findOrdersByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getReceiver());
        assertEquals(1, result.get(0).getItems().size());
        assertEquals(Integer.valueOf(2), result.get(0).getTotalCount());
        assertEquals(59.98, result.get(0).getTotalPrice(), 0.01); // 2 * 2999 / 100
    }

    @Test
    void findOrdersByUserId_shouldReturnEmptyWhenNoOrders() {
        when(orderRepository.findByUserId(999L)).thenReturn(Collections.emptyList());

        List<OrderDTO> result = orderService.findOrdersByUserId(999L);

        assertTrue(result.isEmpty());
    }

    // ========== FIND ORDER BY ID ==========

    @Test
    void findOrderById_shouldReturnOrderWhenFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        OrderDTO result = orderService.findOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("123 Main St", result.getAddress());
    }

    @Test
    void findOrderById_shouldReturnNullWhenNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        OrderDTO result = orderService.findOrderById(999L);

        assertNull(result);
    }

    // ========== CREATE ORDER ==========

    @Test
    void createOrder_shouldSucceedWithValidCart() {
        when(cartRepository.findByUserId(1L)).thenReturn(Arrays.asList(cartItem));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(testOrderItem);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));
        doNothing().when(cartRepository).deleteByUserId(1L);

        OrderDTO result = orderService.createOrder(1L, "John Doe", "123 Main St", "1234567890");

        assertNotNull(result);
        assertEquals("John Doe", result.getReceiver());
        assertEquals("123 Main St", result.getAddress());
        assertEquals("1234567890", result.getTel());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, atLeastOnce()).save(any(OrderItem.class));
        verify(cartRepository, times(1)).deleteByUserId(1L);
    }

    @Test
    void createOrder_shouldReturnNullWhenCartIsEmpty() {
        when(cartRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        OrderDTO result = orderService.createOrder(1L, "John", "Address", "Tel");

        assertNull(result);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_shouldThrowExceptionWhenInsufficientInventory() {
        testBook.setInventory(1); // Only 1 in stock, but cart has 2
        when(cartRepository.findByUserId(1L)).thenReturn(Arrays.asList(cartItem));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        // orderRepository.save() is called once for the order header before inventory check
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.createOrder(1L, "John", "Address", "Tel")
        );

        assertTrue(ex.getMessage().contains("库存不足"));
        // The order IS saved before the inventory validation throws (line 103)
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_shouldHandleNullNumberInCart() {
        cartItem.setNumber(null);
        // OrderItem created with null number (becomes 0)
        OrderItem savedItem = new OrderItem();
        savedItem.setId(1L);
        savedItem.setOrderId(1L);
        savedItem.setBookId(1L);
        savedItem.setBookTitle("Test Book");
        savedItem.setBookCover("cover.jpg");
        savedItem.setNumber(0); // comes from null cart number
        savedItem.setUnitPrice(2999);

        when(cartRepository.findByUserId(1L)).thenReturn(Arrays.asList(cartItem));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(savedItem);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(savedItem));
        doNothing().when(cartRepository).deleteByUserId(1L);

        OrderDTO result = orderService.createOrder(1L, "John", "Address", "Tel");

        assertNotNull(result);
        assertEquals(Integer.valueOf(0), result.getTotalCount());
    }

    @Test
    void createOrder_shouldSkipBookWhenNotFound() {
        Cart cartWithMissingBook = new Cart();
        cartWithMissingBook.setId(2L);
        cartWithMissingBook.setBookId(999L);
        cartWithMissingBook.setUserId(1L);
        cartWithMissingBook.setNumber(1);

        when(cartRepository.findByUserId(1L)).thenReturn(Arrays.asList(cartItem, cartWithMissingBook));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(testOrderItem);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));
        doNothing().when(cartRepository).deleteByUserId(1L);

        OrderDTO result = orderService.createOrder(1L, "John", "Address", "Tel");

        assertNotNull(result);
    }

    // ========== FIND ALL ORDERS ==========

    @Test
    void findAllOrders_shouldReturnAllOrdersSortedByCreatedAtDesc() {
        Order order2 = new Order();
        order2.setId(2L);
        order2.setUserId(2L);
        order2.setReceiver("Jane");
        order2.setCreatedAt(LocalDateTime.of(2026, 6, 1, 12, 0));

        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setOrderId(2L);
        item2.setBookId(2L);
        item2.setBookTitle("Book 2");
        item2.setNumber(1);
        item2.setUnitPrice(1999);

        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder, order2));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));
        when(orderItemRepository.findByOrderId(2L)).thenReturn(Arrays.asList(item2));

        List<OrderDTO> result = orderService.findAllOrders();

        assertEquals(2, result.size());
        // order2 has later createdAt, so it should be first (descending)
        assertEquals("Jane", result.get(0).getReceiver());
        assertEquals("John Doe", result.get(1).getReceiver());
    }

    @Test
    void findAllOrders_shouldReturnEmptyWhenNoOrders() {
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        List<OrderDTO> result = orderService.findAllOrders();

        assertTrue(result.isEmpty());
    }

    // ========== SEARCH ORDERS ==========

    @Test
    void searchOrders_shouldFilterByUserIdAndTimeRange() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2027, 1, 1, 0, 0);

        when(orderRepository.findByUserIdAndTimeRange(1L, start, end))
                .thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        List<OrderDTO> result = orderService.searchOrders(1L, start, end, null);

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getReceiver());
    }

    @Test
    void searchOrders_shouldFilterByUserIdOnly() {
        when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        List<OrderDTO> result = orderService.searchOrders(1L, null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void searchOrders_shouldFilterByBookTitle() {
        when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        List<OrderDTO> result = orderService.searchOrders(1L, null, null, "Test");

        assertEquals(1, result.size());
    }

    @Test
    void searchOrders_shouldExcludeOrdersWithoutMatchingBookTitle() {
        when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        List<OrderDTO> result = orderService.searchOrders(1L, null, null, "NonExistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void searchOrders_shouldHandleNullItemList() {
        Order orderWithNullItems = new Order();
        orderWithNullItems.setId(3L);
        orderWithNullItems.setUserId(1L);
        orderWithNullItems.setCreatedAt(LocalDateTime.now());

        when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList(orderWithNullItems));
        when(orderItemRepository.findByOrderId(3L)).thenReturn(Collections.emptyList());

        List<OrderDTO> result = orderService.searchOrders(1L, null, null, "Test");

        assertTrue(result.isEmpty());
    }

    @Test
    void searchOrders_shouldFilterByTimeRangeOnly() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2027, 1, 1, 0, 0);

        when(orderRepository.findByTimeRange(start, end)).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        List<OrderDTO> result = orderService.searchOrders(null, start, end, null);

        assertEquals(1, result.size());
    }

    @Test
    void searchOrders_shouldReturnAllWhenNoParams() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        List<OrderDTO> result = orderService.searchOrders(null, null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void searchOrders_shouldUseOnlyStartTime() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);

        when(orderRepository.findByUserIdAndTimeRange(eq(1L), eq(start), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        List<OrderDTO> result = orderService.searchOrders(1L, start, null, null);

        assertEquals(1, result.size());
    }

    // ========== SEARCH ALL ORDERS ==========

    @Test
    void searchAllOrders_shouldDelegateToSearchOrdersWithNullUserId() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        List<OrderDTO> result = orderService.searchAllOrders(null, null, null);

        assertEquals(1, result.size());
    }

    // ========== SALES RANKING ==========

    @Test
    void getSalesRanking_shouldReturnBookSalesRanked() {
        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setOrderId(1L);
        item2.setBookId(2L);
        item2.setBookTitle("Book 2");
        item2.setNumber(5);
        item2.setUnitPrice(1999);

        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem, item2));

        List<Map<String, Object>> result = orderService.getSalesRanking(null, null);

        assertEquals(2, result.size());
        // Book 2 has more sales (5) than Test Book (2), should be first
        assertEquals("Book 2", result.get(0).get("bookTitle"));
        assertEquals(5, result.get(0).get("sales"));
        assertEquals("Test Book", result.get(1).get("bookTitle"));
        assertEquals(2, result.get(1).get("sales"));
    }

    @Test
    void getSalesRanking_shouldFilterByTimeRange() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2027, 1, 1, 0, 0);

        when(orderRepository.findByTimeRange(start, end)).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        List<Map<String, Object>> result = orderService.getSalesRanking(start, end);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).get("sales"));
    }

    @Test
    void getSalesRanking_shouldSkipNullBookId() {
        testOrderItem.setBookId(null);
        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        List<Map<String, Object>> result = orderService.getSalesRanking(null, null);

        assertTrue(result.isEmpty());
    }

    // ========== CONSUMPTION RANKING ==========

    @Test
    void getConsumptionRanking_shouldReturnUserSpendingRanked() {
        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setOrderId(1L);
        item2.setBookId(2L);
        item2.setBookTitle("Book 2");
        item2.setNumber(3);
        item2.setUnitPrice(5000); // 50.00

        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem, item2));

        List<Map<String, Object>> result = orderService.getConsumptionRanking(null, null);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).get("userId"));
        // 2 * 2999 + 3 * 5000 = 5998 + 15000 = 20998
        assertEquals(20998L, result.get(0).get("totalAmount"));
        assertEquals("John Doe", result.get(0).get("nickname"));
    }

    @Test
    void getConsumptionRanking_shouldSkipNullUserId() {
        testOrder.setUserId(null);
        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder));

        List<Map<String, Object>> result = orderService.getConsumptionRanking(null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getConsumptionRanking_shouldFilterByTimeRange() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2027, 1, 1, 0, 0);

        when(orderRepository.findByTimeRange(start, end)).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        List<Map<String, Object>> result = orderService.getConsumptionRanking(start, end);

        assertEquals(1, result.size());
        assertEquals(5998L, result.get(0).get("totalAmount")); // 2 * 2999
    }

    // ========== PERSONAL STATISTICS ==========

    @Test
    void getPersonalStatistics_shouldReturnUserStats() {
        when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        Map<String, Object> result = orderService.getPersonalStatistics(1L, null, null);

        assertNotNull(result);
        assertEquals(2, result.get("totalBooks")); // Total books = number
        assertEquals(5998L, result.get("totalAmount")); // 2 * 2999
        List<?> details = (List<?>) result.get("details");
        assertEquals(1, details.size());
    }

    @Test
    void getPersonalStatistics_shouldFilterByTimeRange() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2027, 1, 1, 0, 0);

        when(orderRepository.findByUserIdAndTimeRange(1L, start, end))
                .thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        Map<String, Object> result = orderService.getPersonalStatistics(1L, start, end);

        assertNotNull(result);
        assertEquals(2, result.get("totalBooks"));
    }

    @Test
    void getPersonalStatistics_shouldHandleNullNumbers() {
        testOrderItem.setNumber(null);
        testOrderItem.setUnitPrice(null);

        when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        Map<String, Object> result = orderService.getPersonalStatistics(1L, null, null);

        assertEquals(0, result.get("totalBooks"));
        assertEquals(0L, result.get("totalAmount"));
    }

    // ========== ORDER DTO MAPPING EDGE CASES ==========

    @Test
    void toOrderDTO_shouldHandleNullFieldsInOrderItem() {
        testOrderItem.setNumber(null);
        testOrderItem.setUnitPrice(null);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        OrderDTO result = orderService.findOrderById(1L);

        assertNotNull(result);
        assertEquals(Integer.valueOf(0), result.getTotalCount());
        assertEquals(0.0, result.getTotalPrice(), 0.01);
    }

    @Test
    void createOrder_shouldHandleNullInventory() {
        testBook.setInventory(null);
        when(cartRepository.findByUserId(1L)).thenReturn(Arrays.asList(cartItem));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.createOrder(1L, "John", "Address", "Tel")
        );

        assertTrue(ex.getMessage().contains("库存不足"));
    }
}