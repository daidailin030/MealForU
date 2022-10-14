# MealForU
一個基於現有食材做搜尋的食譜App，首頁以備忘錄方式記錄食材以便進行搜尋，也在其中提供了計時器功能滿足料理時的需求。在輸入方面提供了如紅蘿蔔、雞蛋、鯛魚等等十多種食材讓使用者能夠以辨識功能來輸入，更增添了使用的趣味性。


![Alt text](https://github.com/ttom1224/mealforU/blob/master/graguate/src/Screenshot_20210524-225032.jpg)
![Alt text](https://github.com/ttom1224/mealforU/blob/master/graguate/src/Screenshot_20210524-224519.jpg)
![Alt text](https://github.com/ttom1224/mealforU/blob/master/graguate/src/Screenshot_20210524-224754.jpg)
![Alt text](https://github.com/ttom1224/mealforU/blob/master/graguate/src/Screenshot_20210524-224941.jpg)

****
## Features
我們使用了Firebase來儲存使用者的資料與使用習慣如點擊過的食譜與相關內容等等來改善食譜的推薦與排序，希望能提供最佳的使用者體驗。此外透過google vision api來實現多種食材辨識的功能，讓使用者能夠有不同的輸入體驗。

## 資料庫
我們使用Firebase作為資料庫，用來存放食譜、備忘錄、使用者資訊等等。

## Architecture
系統功能架構
|  主要頁面   | 功能  |
|  ----  | ----  |
|  首頁 | 顯示與新增備忘錄 |
| 食譜頁  | 顯示推薦食譜與熱門食譜 |
| 搜尋頁  | 進行查詢與顯示搜尋結果 |
| 辨識頁面  | 進行食材圖像辨識<br>增加至備忘錄<br>進行查詢|
| 功能側選單  | 登入<br>計時器<br>收藏<br>拍照教學<br>問題反映 |
| 備忘錄  | 察看與管理備忘錄內容 |

PS：食譜取自MASA老師。
