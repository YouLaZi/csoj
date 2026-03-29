import axios from "axios";

const API_BASE = "/api";

/**
 * 猫咪养成服务
 */
class CatService {
  /**
   * 获取用户猫咪
   */
  async getCat() {
    const response = await axios.get(`${API_BASE}/cat`);
    return response.data;
  }

  /**
   * 喂食猫咪
   * @param itemCode 物品代码
   */
  async feedCat(itemCode: string) {
    const response = await axios.post(`${API_BASE}/cat/feed`, null, {
      params: { itemCode },
    });
    return response.data;
  }

  /**
   * 与猫咪玩耍
   * @param toyCode 玩具代码
   */
  async playWithCat(toyCode: string) {
    const response = await axios.post(`${API_BASE}/cat/play`, null, {
      params: { toyCode },
    });
    return response.data;
  }

  /**
   * 抚摸猫咪
   */
  async petCat() {
    const response = await axios.post(`${API_BASE}/cat/pet`);
    return response.data;
  }

  /**
   * 让猫咪睡觉
   */
  async sleepCat() {
    const response = await axios.post(`${API_BASE}/cat/sleep`);
    return response.data;
  }

  /**
   * 设置猫咪名字
   * @param name 猫咪名字
   */
  async setCatName(name: string) {
    const response = await axios.put(`${API_BASE}/cat/name`, null, {
      params: { name },
    });
    return response.data;
  }

  /**
   * 获取商店物品
   * @param itemType 物品类型
   */
  async getShopItems(itemType?: string) {
    const response = await axios.get(`${API_BASE}/cat/shop`, {
      params: { itemType },
    });
    return response.data;
  }

  /**
   * 购买物品
   * @param itemCode 物品代码
   * @param quantity 数量
   */
  async buyItem(itemCode: string, quantity: number = 1) {
    const response = await axios.post(`${API_BASE}/cat/buy`, null, {
      params: { itemCode, quantity },
    });
    return response.data;
  }

  /**
   * 获取用户物品背包
   */
  async getInventory() {
    const response = await axios.get(`${API_BASE}/cat/inventory`);
    return response.data;
  }
}

export default new CatService();
