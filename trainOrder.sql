if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[trainOrder]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[trainOrder]
GO

CREATE TABLE [dbo].[trainOrder] (
	[train_number] [int] NULL ,
	[seriary_number] [int] NULL ,
	[car_number] [varchar] (30) COLLATE Chinese_PRC_CI_AS NULL ,
	[car_marque] [varchar] (30) COLLATE Chinese_PRC_CI_AS NULL ,
	[carry_weight1] [numeric](9, 3) NULL ,
	[self_weight1] [numeric](9, 3) NULL ,
	[past_time] [nvarchar] (50) COLLATE Chinese_PRC_CI_AS NULL ,
	[outFlag] [nvarchar] (50) COLLATE Chinese_PRC_CI_AS NULL ,
	[badFlag] [bit] DEFAULT (1) NOT NULL ,
	[year_level2] [varchar] (30) COLLATE Chinese_PRC_CI_AS NULL ,
	[month_level3] [varchar] (30) COLLATE Chinese_PRC_CI_AS NULL ,
	[sn] [int] IDENTITY (1001, 1) NOT NULL 
) ON [PRIMARY]
GO