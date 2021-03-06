package com.taotao.manage.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taotao.common.service.ApiService;
import com.taotao.manage.mapper.ItemMapper;
import com.taotao.manage.pojo.Item;
import com.taotao.manage.pojo.ItemDesc;
import com.taotao.manage.pojo.ItemParamItem;

@Service
public class ItemService extends BaseService<Item> {

    // 在service中springmvc自动开启事物管理
    // 事物具有传播性，itemMDescService存在于ItemService中，所以说他们执行的方法在同一个事物中
    @Autowired
    private ItemDescService iteMDescService;

    @Autowired
    private ItemParamItemService itemParamItemService;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ApiService apiService;

    @Value("${TAOTAO_WEB_UTL}")
    private String TAOTAO_WEB_UTL;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void saveItem(Item item, String desc, String itemParams) {
	// 设置初始值
	item.setStatus(1);
	item.setId(null); // 安全考虑，强制设置id为null
	super.save(item);
	ItemDesc ite = new ItemDesc();
	ite.setItemId(item.getId());
	ite.setItemDesc(desc);
	iteMDescService.save(ite);
	// 保存规格参数数据
	ItemParamItem itemParamItem = new ItemParamItem();
	itemParamItem.setItemId(item.getId());
	itemParamItem.setParamData(itemParams);
	this.itemParamItemService.save(itemParamItem);

	// 发送消息
	sendMsg(item.getId(), "insert");
    }

    /**
     * @Title queryPageList
     * @Description TODO
     * @param page
     * @param rows void
     * @throws 
     */
    public PageInfo<Item> queryPageList(Integer page, Integer rows) {
	Example example = new Example(Item.class);
	example.setOrderByClause("updated DESC");
	// 设置分页参数 一定要放在查询前面
	PageHelper.startPage(page, rows);
	List<Item> list = itemMapper.selectByExample(example);
	return new PageInfo<Item>(list);
    }

    /**
     * @Title updateItem
     * @Description 修改商品
     * @param item
     * @param desc void
     * @throws 
     */
    public void updateItem(Item item, String desc, String itemParams) {
	// 强制设置不能修改的字段为null
	item.setStatus(null);
	item.setCreated(null);
	super.updateSelective(item); // 选择不为null的字段作为修改
	// 修改商品描述数据
	ItemDesc itemDesc = new ItemDesc();
	itemDesc.setItemId(item.getId());
	itemDesc.setItemDesc(desc);
	this.iteMDescService.updateSelective(itemDesc);
	// 修改商品对应的规格参数
	this.itemParamItemService.udpateItemParamItem(item.getId(), itemParams);

	/*
	 * 通知前台系统其他系统商品已经更新了(调用前台对缓存的处理接口，这里前台是直接删除缓存的)
	 * 在service中不能trycatch，如果捕获异常了，就会影响事物的回滚
	 * 但是这里的逻辑是不属于正常逻辑的一部分(不需要事物回滚)，这里可以trycatch
	 */
	/*
	 * try { String url = TAOTAO_WEB_UTL + "/item/cache/" + item.getId() +
	 * ".html"; this.apiService.doPost(url, null); } catch (Exception e) {
	 * e.printStackTrace(); }
	 */
	/*
	 * 使用rabbitmq消息队列通知前台，代替上面的功能
	 */
	sendMsg(item.getId(), "update");
    }

    private void sendMsg(Long itemId, String type) {
	try {
	    Map<String, Object> msg = new HashMap<String, Object>();
	    msg.put("itemId", itemId);
	    msg.put("type", type);
	    msg.put("date", System.currentTimeMillis()); // 当前时间戳
	    // 参数：1.使用通配符方式通知 2.数据以jso格式传输
	    this.rabbitTemplate.convertAndSend("item." + type, MAPPER.writeValueAsString(msg));
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
