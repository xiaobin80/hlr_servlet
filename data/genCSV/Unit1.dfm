object Form1: TForm1
  Left = 192
  Top = 128
  Width = 870
  Height = 500
  Caption = 'Form1'
  Color = clBtnFace
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clWindowText
  Font.Height = -11
  Font.Name = 'MS Sans Serif'
  Font.Style = []
  OldCreateOrder = False
  PixelsPerInch = 96
  TextHeight = 13
  object Label1: TLabel
    Left = 136
    Top = 152
    Width = 61
    Height = 13
    Caption = 'Table Name:'
  end
  object Label2: TLabel
    Left = 152
    Top = 112
    Width = 43
    Height = 13
    Caption = 'DB Path:'
  end
  object Label3: TLabel
    Left = 120
    Top = 192
    Width = 77
    Height = 13
    Caption = 'Export File Path:'
  end
  object Button1: TButton
    Left = 208
    Top = 240
    Width = 75
    Height = 25
    Caption = 'gen'
    TabOrder = 0
    OnClick = Button1Click
  end
  object edtDBPath: TEdit
    Left = 208
    Top = 112
    Width = 241
    Height = 21
    TabOrder = 1
    Text = 'E:\j_build\sjz\VeicRfidCps.mdb'
  end
  object edtTlbName: TEdit
    Left = 208
    Top = 152
    Width = 241
    Height = 21
    TabOrder = 2
    Text = 'traintotalh'
  end
  object edtExpPath: TEdit
    Left = 208
    Top = 192
    Width = 241
    Height = 21
    TabOrder = 3
    Text = 'E:\trainOrder.txt'
  end
  object ADOConnection1: TADOConnection
    Left = 144
    Top = 72
  end
end
