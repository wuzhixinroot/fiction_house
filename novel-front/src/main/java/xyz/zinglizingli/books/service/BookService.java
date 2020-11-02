package xyz.zinglizingli.books.service;

import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
<<<<<<< HEAD
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
=======
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
>>>>>>> update
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.orderbyhelper.OrderByHelper;
import xyz.zinglizingli.books.core.constant.CacheKeyConstans;
import xyz.zinglizingli.books.core.enums.PicSaveType;
<<<<<<< HEAD
import xyz.zinglizingli.books.mapper.*;
import xyz.zinglizingli.books.po.*;
import xyz.zinglizingli.books.core.utils.Constants;
import xyz.zinglizingli.common.utils.SpringUtil;
import xyz.zinglizingli.common.utils.UUIDUtils;
import xyz.zinglizingli.common.cache.CommonCacheUtil;
import xyz.zinglizingli.common.utils.RestTemplateUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
=======
import xyz.zinglizingli.books.core.utils.Constants;
import xyz.zinglizingli.books.mapper.*;
import xyz.zinglizingli.books.po.*;
import xyz.zinglizingli.common.cache.CommonCacheUtil;
import xyz.zinglizingli.common.utils.FileUtil;
import xyz.zinglizingli.common.utils.SpringUtil;

>>>>>>> update
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author XXY
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookMapper bookMapper;

    private final BookIndexMapper bookIndexMapper;

    private final BookContentMapper bookContentMapper;

    private final ScreenBulletMapper screenBulletMapper;

    private final UserRefBookMapper userRefBookMapper;

<<<<<<< HEAD
=======
    private final BookParseLogMapper bookParseLogMapper;

    private final BookUpdateTimeLogMapper bookUpdateTimeLogMapper;

>>>>>>> update
    private final CommonCacheUtil cacheUtil;


    @Value("${pic.save.type}")
    private Integer picSaveType;

    @Value("${pic.save.path}")
    private String picSavePath;


<<<<<<< HEAD

    /**
     * 保存章节目录和内容
     * */
    public void saveBookAndIndexAndContent(Book book, List<BookIndex> bookIndex, List<BookContent> bookContent){
=======
    /**
     * 保存章节目录和内容
     */
    public void saveBookAndIndexAndContent(Book book, List<BookIndex> bookIndex, List<BookContent> bookContent) {
>>>>>>> update
        //解决内部调用事物不生效的问题
        BookService bookService = SpringUtil.getBean(BookService.class);

        boolean isUpdate = false;
        Long bookId = -1L;
        book.setBookName(book.getBookName().trim());
        book.setAuthor(book.getAuthor().trim());
        BookExample example = new BookExample();
        example.createCriteria().andBookNameEqualTo(book.getBookName()).andAuthorEqualTo(book.getAuthor());
        List<Book> books = bookMapper.selectByExample(example);
        if (books.size() > 0) {
            //更新
            bookId = books.get(0).getId();
<<<<<<< HEAD
=======
            if (picSaveType == PicSaveType.LOCAL.getValue() && books.get(0).getPicUrl().startsWith(Constants.LOCAL_PIC_PREFIX)) {
                book.setPicUrl(null);
            }
>>>>>>> update
            updateBook(book, bookId);
            isUpdate = true;

        } else {
            //插入
            if (book.getVisitCount() == null) {
                Long visitCount = generateVisitCount(book.getScore());
                book.setVisitCount(visitCount);
            }
            int rows = bookMapper.insertSelective(book);
            if (rows > 0) {
                bookId = book.getId();
            }


        }

        if (bookId >= 0) {

            List<BookIndex> newBookIndexList = new ArrayList<>();
            List<BookContent> newContentList = new ArrayList<>();
            for (int i = 0; i < bookIndex.size(); i++) {
                BookContent bookContentItem = bookContent.get(i);
                if (!bookContentItem.getContent().contains(Constants.NO_CONTENT_DESC)) {
                    BookIndex bookIndexItem = bookIndex.get(i);
                    bookIndexItem.setBookId(bookId);
                    bookContentItem.setBookId(bookId);
                    bookContentItem.setIndexNum(bookIndexItem.getIndexNum());
                    newBookIndexList.add(bookIndexItem);
                    newContentList.add(bookContentItem);
                }
<<<<<<< HEAD
                //一次最多只允许插入100条记录,否则影响服务器响应
                if (isUpdate && i % 100 == 0 && newBookIndexList.size() > 0) {
=======
                //一次最多只允许插入50条记录,否则影响服务器响应
                if (isUpdate && i % 50 == 0 && newBookIndexList.size() > 0) {
>>>>>>> update
                    bookService.insertIndexListAndContentList(newBookIndexList, newContentList);
                    newBookIndexList = new ArrayList<>();
                    newContentList = new ArrayList<>();
                    try {
<<<<<<< HEAD
                        Thread.sleep(1000 * 60 * 1);
=======
                        Thread.sleep(1000 * 30);
>>>>>>> update
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                        throw new RuntimeException(e.getMessage());
                    }
                }
            }


            if (newBookIndexList.size() > 0) {
                bookService.insertIndexListAndContentList(newBookIndexList, newContentList);
            }

<<<<<<< HEAD
            cacheUtil.del(CacheKeyConstans.NEWST_BOOK_LIST_KEY);

=======
>>>>>>> update

        }


    }

    /**
     * 更新书籍
<<<<<<< HEAD
     * */
    private void updateBook(Book book, Long bookId) {
        book.setId(bookId);
        String picSrc = book.getPicUrl();
        if(picSaveType == PicSaveType.LOCAL.getValue() && StringUtils.isNotBlank(picSrc)){
            try {
                //本地图片保存
                HttpHeaders headers = new HttpHeaders();
                HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
                ResponseEntity<Resource> resEntity = RestTemplateUtil.getInstance(Charsets.ISO_8859_1).exchange(picSrc, HttpMethod.GET, requestEntity, Resource.class);
                InputStream input = Objects.requireNonNull(resEntity.getBody()).getInputStream();
                Date currentDate = new Date();
                picSrc = "/localPic/" + DateUtils.formatDate(currentDate, "yyyy") + "/" + DateUtils.formatDate(currentDate, "MM") + "/" + DateUtils.formatDate(currentDate, "dd")
                        + UUIDUtils.getUUID32()
                        + picSrc.substring(picSrc.lastIndexOf("."));
                File picFile = new File(picSavePath + picSrc);
                File parentFile = picFile.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                OutputStream out = new FileOutputStream(picFile);
                byte[] b = new byte[4096];
                for (int n; (n = input.read(b)) != -1; ) {
                    out.write(b, 0, n);
                }
                out.close();
                input.close();
                book.setPicUrl(picSrc);
            }catch (Exception e){
                log.error(e.getMessage(),e);
=======
     */
    public void updateBook(Book book, Long bookId) {
        book.setId(bookId);
        String picSrc = book.getPicUrl();
        if (picSaveType == PicSaveType.LOCAL.getValue() && StringUtils.isNotBlank(picSrc)) {
            try {
                picSrc = FileUtil.network2Local(picSrc, picSavePath);
                book.setPicUrl(picSrc);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
>>>>>>> update
            }

        }
        bookMapper.updateByPrimaryKeySelective(book);
    }

    /**
<<<<<<< HEAD
     * 批量插入章节目录表和章节内容表（自动修复错误章节）
     * */
    @Transactional(rollbackFor = Exception.class)
    public void insertIndexListAndContentList(List<BookIndex> newBookIndexList, List<BookContent> newContentList) {
        long start = System.currentTimeMillis();
        if(newBookIndexList.size() > 0) {
=======
     * 网络图片转本地
     *
     * @param book
     */
    public void networkPicToLocal(Book book) {
        try {
            book.setPicUrl(FileUtil.network2Local(book.getPicUrl(), picSavePath));
            bookMapper.updateByPrimaryKeySelective(book);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }


    /**
     * 批量插入章节目录表和章节内容表（自动修复错误章节）
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertIndexListAndContentList(List<BookIndex> newBookIndexList, List<BookContent> newContentList) {
        long start = System.currentTimeMillis();
        if (newBookIndexList.size() > 0) {
>>>>>>> update
            //删除已存在的错误章节
            List<Integer> indexNumberList = newBookIndexList.stream().map(BookIndex::getIndexNum).collect(Collectors.toList());
            Long bookId = newBookIndexList.get(0).getBookId();
            BookIndexExample bookIndexExample = new BookIndexExample();
            bookIndexExample.createCriteria().andBookIdEqualTo(bookId).andIndexNumIn(indexNumberList);
            bookIndexMapper.deleteByExample(bookIndexExample);
            BookContentExample bookContentExample = new BookContentExample();
            bookContentExample.createCriteria().andBookIdEqualTo(bookId).andIndexNumIn(indexNumberList);
            bookContentMapper.deleteByExample(bookContentExample);

            //插入新的章节
            bookIndexMapper.insertBatch(newBookIndexList);
            bookContentMapper.insertBatch(newContentList);
        }
<<<<<<< HEAD
        log.info("更新章节耗时："+(System.currentTimeMillis()-start));
=======
        log.info("更新章节耗时：" + (System.currentTimeMillis() - start));
>>>>>>> update
    }


    /**
     * 生成随机访问次数
<<<<<<< HEAD
     * */
    private Long generateVisitCount(Float score) {
        int baseNum = (int)(score * 100);
=======
     */
    private Long generateVisitCount(Float score) {
        int baseNum = (int) (score * 100);
>>>>>>> update
        return Long.parseLong(baseNum + new Random().nextInt(1000) + "");
    }

    /**
     * 分页查询
     */
    public List<Book> search(int page, int pageSize,
                             String userId, String ids, String keyword, String bookStatus, Integer catId, Integer softCat, String softTag, String sortBy, String sort) {

        if (!StringUtils.isEmpty(userId)) {
            sortBy = "user_ref_book.id";
            sort = "DESC";
        }
        PageHelper.startPage(page, pageSize);
        // 排序设置[注意orderby 紧跟分页后面]
        if (!StringUtils.isEmpty(sortBy)) {
            OrderByHelper.orderBy(sortBy + " " + sort);
        }

        return bookMapper.search(userId, ids, keyword, catId, softCat, softTag, bookStatus);

    }


<<<<<<< HEAD

    /**
     * 查询书籍的基础数据
     * */
=======
    /**
     * 查询书籍的基础数据
     */
>>>>>>> update
    public Book queryBaseInfo(Long bookId) {

        return bookMapper.selectByPrimaryKey(bookId);
    }

    /**
     * 查询最新更新的书籍列表
<<<<<<< HEAD
     * */
=======
     */
>>>>>>> update
    public List<BookIndex> queryNewIndexList(Long bookId) {
        PageHelper.startPage(1, 15);
        BookIndexExample example = new BookIndexExample();
        example.createCriteria().andBookIdEqualTo(bookId);
        example.setOrderByClause("index_num DESC");
        return bookIndexMapper.selectByExample(example);

    }

    /**
     * 查询书籍目录列表
<<<<<<< HEAD
     * */
=======
     */
>>>>>>> update
    public List<BookIndex> queryAllIndexList(Long bookId) {
        BookIndexExample example = new BookIndexExample();
        example.createCriteria().andBookIdEqualTo(bookId);
        example.setOrderByClause("index_num ASC");
        return bookIndexMapper.selectByExample(example);
    }

    /**
     * 查询书籍章节内容
<<<<<<< HEAD
     * */
=======
     */
>>>>>>> update
    public BookContent queryBookContent(Long bookId, Integer indexNum) {
        BookContent content = (BookContent) cacheUtil.getObject(CacheKeyConstans.BOOK_CONTENT_KEY_PREFIX + "_" + bookId + "_" + indexNum);
        if (content == null) {
            BookContentExample example = new BookContentExample();
            example.createCriteria().andBookIdEqualTo(bookId).andIndexNumEqualTo(indexNum);
            List<BookContent> bookContents = bookContentMapper.selectByExample(example);
            content = bookContents.size() > 0 ? bookContents.get(0) : null;
<<<<<<< HEAD
            cacheUtil.setObject(CacheKeyConstans.BOOK_CONTENT_KEY_PREFIX + "_" + bookId + "_" + indexNum, content, 60 * 60 * 24);
=======
            cacheUtil.setObject(CacheKeyConstans.BOOK_CONTENT_KEY_PREFIX + "_" + bookId + "_" + indexNum, content, 60 * 10);
>>>>>>> update
        }

        return content;
    }


    /**
     * 增加访问次数
<<<<<<< HEAD
     * */
=======
     */
>>>>>>> update
    public void addVisitCount(Long bookId, String userId, Integer indexNum) {

        bookMapper.addVisitCount(bookId);

<<<<<<< HEAD
        if(org.apache.commons.lang3.StringUtils.isNotBlank(userId)) {
=======
        if (org.apache.commons.lang3.StringUtils.isNotBlank(userId)) {
>>>>>>> update
            userRefBookMapper.updateNewstIndex(bookId, userId, indexNum);
        }

    }

    /**
     * 查询章节名
<<<<<<< HEAD
     * */
=======
     */
>>>>>>> update
    public String queryIndexNameByBookIdAndIndexNum(Long bookId, Integer indexNum) {

        BookIndexExample example = new BookIndexExample();
        example.createCriteria().andBookIdEqualTo(bookId).andIndexNumEqualTo(indexNum);
        List<BookIndex> indexList = bookIndexMapper.selectByExample(example);
<<<<<<< HEAD
        if(indexList != null && indexList.size() > 0 ) {
=======
        if (indexList != null && indexList.size() > 0) {
>>>>>>> update
            return indexList.get(0).getIndexName();
        }
        return null;
    }

    /**
     * 查询最大和最小章节号
<<<<<<< HEAD
     * */
=======
     */
>>>>>>> update
    public List<Integer> queryMaxAndMinIndexNum(Long bookId) {
        List<Integer> result = new ArrayList<>();
        BookIndexExample example = new BookIndexExample();
        example.createCriteria().andBookIdEqualTo(bookId);
        example.setOrderByClause("index_num desc");
        List<BookIndex> bookIndices = bookIndexMapper.selectByExample(example);
        if (bookIndices.size() > 0) {
            result.add(bookIndices.get(0).getIndexNum());
            result.add(bookIndices.get(bookIndices.size() - 1).getIndexNum());
        }
        return result;
    }

    /**
     * 查询该书籍已存在目录号
     */
<<<<<<< HEAD
    public Map<Integer,BookIndex> queryIndexByBookNameAndAuthor(String bookName, String author) {
=======
    public Map<Integer, BookIndex> queryIndexByBookNameAndAuthor(String bookName, String author) {
>>>>>>> update
        BookExample example = new BookExample();
        example.createCriteria().andBookNameEqualTo(bookName).andAuthorEqualTo(author);
        List<Book> books = bookMapper.selectByExample(example);
        if (books.size() > 0) {

            Long bookId = books.get(0).getId();
            BookIndexExample bookIndexExample = new BookIndexExample();
            bookIndexExample.createCriteria().andBookIdEqualTo(bookId);
            List<BookIndex> bookIndices = bookIndexMapper.selectByExample(bookIndexExample);
<<<<<<< HEAD
            if(bookIndices.size() > 0) {
=======
            if (bookIndices.size() > 0) {
>>>>>>> update
                return bookIndices.stream().collect(Collectors.toMap(BookIndex::getIndexNum, Function.identity()));
            }

        }

        return new HashMap<>(0);

    }


<<<<<<< HEAD


    /**
     * 保存弹幕
     * */
=======
    /**
     * 保存弹幕
     */
>>>>>>> update
    public void sendBullet(Long contentId, String bullet) {

        ScreenBullet screenBullet = new ScreenBullet();
        screenBullet.setContentId(contentId);
        screenBullet.setScreenBullet(bullet);
        screenBullet.setCreateTime(new Date());

        screenBulletMapper.insertSelective(screenBullet);
    }

    /**
     * 查询弹幕
<<<<<<< HEAD
     * */
=======
     */
>>>>>>> update
    public List<ScreenBullet> queryBullet(Long contentId) {

        ScreenBulletExample example = new ScreenBulletExample();
        example.createCriteria().andContentIdEqualTo(contentId);
        example.setOrderByClause("create_time asc");

        return screenBulletMapper.selectByExample(example);
    }


    /**
     * 查询章节内容
<<<<<<< HEAD
     * */
=======
     */
>>>>>>> update
    public String queryContentList(Long bookId, int count) {
        BookContentExample example = new BookContentExample();
        example.createCriteria().andBookIdEqualTo(bookId).andIndexNumEqualTo(count);
        return bookContentMapper.selectByExample(example).get(0).getContent();
    }

    /**
     * 查询章节数
<<<<<<< HEAD
     * */
=======
     */
>>>>>>> update
    public int countIndex(Long bookId) {
        BookIndexExample example = new BookIndexExample();
        example.createCriteria().andBookIdEqualTo(bookId);
        return bookIndexMapper.countByExample(example);
    }


<<<<<<< HEAD


    /**
     * 查询前一章节和后一章节号
     * */
=======
    /**
     * 查询前一章节和后一章节号
     */
>>>>>>> update
    public List<Integer> queryPreAndNextIndexNum(Long bookId, Integer indexNum) {
        List<Integer> result = new ArrayList<>();
        BookIndexExample example = new BookIndexExample();
        example.createCriteria().andBookIdEqualTo(bookId).andIndexNumGreaterThan(indexNum);
        example.setOrderByClause("index_num asc");
        List<BookIndex> bookIndices = bookIndexMapper.selectByExample(example);
        if (bookIndices.size() > 0) {
            result.add(bookIndices.get(0).getIndexNum());
        } else {
            result.add(indexNum);
        }
        example = new BookIndexExample();
        example.createCriteria().andBookIdEqualTo(bookId).andIndexNumLessThan(indexNum);
        example.setOrderByClause("index_num DESC");
        bookIndices = bookIndexMapper.selectByExample(example);
        if (bookIndices.size() > 0) {
            result.add(bookIndices.get(0).getIndexNum());
        } else {
            result.add(indexNum);
        }
        return result;

    }

    /**
     * 查询推荐书籍数据
<<<<<<< HEAD
     * */
=======
     */
>>>>>>> update
    public List<Book> queryRecBooks(List<Map<String, String>> configMap) {
        return bookMapper.queryRecBooks(configMap);
    }

    /**
     * 清理数据库中无效数据
<<<<<<< HEAD
     * */
=======
     */
>>>>>>> update
    public void clearInvilidData() {

        //清除无效内容
        bookContentMapper.clearInvilidContent();

        //清除无效章节
        bookIndexMapper.clearInvilidIndex();

        //清楚无效书籍
        bookMapper.clearInvilidBook();
    }
<<<<<<< HEAD
=======

    /**
     * 查询网络图片的小说
     *
     * @param limit
     * @param offset
     */
    public List<Book> queryNetworkPicBooks(Integer limit, Integer offset) {
        return bookMapper.queryNetworkPicBooks(limit, offset);
    }

    /**
     * 通过图片名查询小说数量
     */
    public int countByPicName(String fileName) {
        BookExample bookExample = new BookExample();
        bookExample.createCriteria().andPicUrlLike('%' + fileName + '%');
        return bookMapper.countByExample(bookExample);
    }

    /**
     * 添加解析日志
     */
    public void addBookParseLog(String bookUrl, String bookName, Float score, Byte priority) {
        BookParseLogExample example = new BookParseLogExample();
        example.createCriteria().andBookUrlEqualTo(bookUrl).andCreateTimeGreaterThan(new Date(System.currentTimeMillis() - 1000 * 60 * 60));
        if (bookParseLogMapper.countByExample(example) == 0) {
            BookParseLog bookParseLog = new BookParseLog();
            bookParseLog.setBookUrl(bookUrl);
            bookParseLog.setBookName(bookName);
            bookParseLog.setScore(score);
            bookParseLog.setPriority(priority);
            bookParseLog.setCreateTime(new Date());
            bookParseLogMapper.insertSelective(bookParseLog);
        }
    }

    /**
     * 查询解析日志
     */
    public List<BookParseLog> queryBookParseLogs() {
        List<BookParseLog> logs = bookParseLogMapper.queryBookParseLogs();
        if(logs.size()>0) {
            SpringUtil.getBean(BookService.class).addBookUpdateCount(logs);
        }
        return logs;
    }

    /**
     * 增加小说更新次数
     */
    @Async
    public void addBookUpdateCount(List<BookParseLog> logs) {
        bookParseLogMapper.addBookUpdateCount(logs);
    }

    /**
     * 删除已经成功更新的解析日志
     */
    public void deleteBookParseLogs(List<Long> successLogIds) {
        if (successLogIds.size() > 0) {
            BookParseLogExample example = new BookParseLogExample();
            example.createCriteria().andIdIn(successLogIds);
            bookParseLogMapper.deleteByExample(example);
        }
    }

    /**
     * 查询书籍是否存在
     */
    public Boolean hasBook(String bookName, String author) {
        BookExample example = new BookExample();
        example.createCriteria().andBookNameEqualTo(bookName).andAuthorEqualTo(author);
        return bookMapper.countByExample(example) > 0;
    }

    /**
     * 查询分类更新时间映射信息
     */
    public Map<Integer, Date> queryLastUpdateTime() {
        List<BookUpdateTimeLog> list = bookUpdateTimeLogMapper.selectByExample(new BookUpdateTimeLogExample());

        return list.stream().collect(Collectors.toMap(BookUpdateTimeLog::getBookCatId, BookUpdateTimeLog::getLastUpdateTime, (key1, key2) -> key2));

    }

    /**
     * 更新分类时间日志
     */
    public void updateBookUpdateTimeLog(Map<Integer, Date> cat2Date) {
        if (cat2Date.size() > 0) {
            Set<Map.Entry<Integer, Date>> entries = cat2Date.entrySet();
            for (Map.Entry<Integer, Date> entry : entries) {
                BookUpdateTimeLogExample example = new BookUpdateTimeLogExample();
                example.createCriteria().andBookCatIdEqualTo(entry.getKey());
                BookUpdateTimeLog entity = new BookUpdateTimeLog();
                entity.setLastUpdateTime(entry.getValue());
                bookUpdateTimeLogMapper.updateByExampleSelective(entity, example);

            }
        }
    }

    /**
     * 删除已经成功更新的解析日志
     */
    public void deleteBookParseLog(Long id) {
        bookParseLogMapper.deleteByPrimaryKey(id);

    }

    /**
     * 查询数据库书籍数量
     */
    public int queryBookNumber() {

        Integer bookNumber = (Integer) cacheUtil.getObject(CacheKeyConstans.BOOK_NUMBER_KEY);
        if (bookNumber == null) {
            bookNumber = bookMapper.countByExample(new BookExample());
            cacheUtil.setObject(CacheKeyConstans.BOOK_NUMBER_KEY, bookNumber, 60 * 5);
        }
        return bookNumber;
    }
>>>>>>> update
}
