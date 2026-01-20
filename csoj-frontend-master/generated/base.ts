/* tslint:disable */
/* eslint-disable */
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, ResponseType } from 'axios';

export type QueryParamsType = Record<string | number, any>;

export interface FullRequestParams extends Omit<AxiosRequestConfig, 'data' | 'params' | 'url' | 'responseType'> {
  /** set parameter to `true` for call `securityWorker` for this request */
  secure?: boolean;
  /** request path */
  path: string;
  /** content type of request body */
  type?: ContentType;
  /** query params */
  query?: QueryParamsType;
  /** format of response (i.e. response.json() -> format: "json") */
  format?: ResponseType;
  /** request body */
  body?: unknown;
}

export type RequestParams = Omit<FullRequestParams, 'body' | 'method' | 'query' | 'path'>;

export interface ApiConfig extends Omit<AxiosRequestConfig, 'data' | 'cancelToken'> {
  securityWorker?: (securityData: any) => Promise<AxiosRequestConfig | void> | AxiosRequestConfig | void;
  secure?: boolean;
  format?: ResponseType;
}

export enum ContentType {
  Json = 'application/json',
  FormData = 'multipart/form-data',
  UrlEncoded = 'application/x-www-form-urlencoded',
}

export class HttpClient<SecurityDataType = unknown> {
  public instance: AxiosInstance;
  private securityData: SecurityDataType | null = null;
  private securityWorker?: ApiConfig['securityWorker'];
  private secure?: boolean;
  private format?: ResponseType;

  constructor({ securityWorker, secure, format, ...axiosConfig }: ApiConfig = {}) {
    this.instance = axios.create({ ...axiosConfig, baseURL: axiosConfig.baseURL || '' });
    this.secure = secure;
    this.format = format;
    this.securityWorker = securityWorker;
  }

  public setSecurityData = (data: SecurityDataType | null) => {
    this.securityData = data;
  };

  private mergeRequestParams(params1: AxiosRequestConfig, params2?: AxiosRequestConfig): AxiosRequestConfig {
    // 创建一个空的AxiosHeaders实例
    const headers = new axios.AxiosHeaders();
    
    // 逐个添加headers，避免使用展开运算符
    const defaultHeaders = this.instance.defaults.headers || {};
    const params1Headers = params1.headers || {};
    const params2Headers = params2 && params2.headers ? params2.headers : {};
    
    // 将各个header对象的属性复制到AxiosHeaders实例中
    Object.entries(defaultHeaders).forEach(([key, value]) => {
      if (value !== undefined) headers.set(key, value);
    });
    
    Object.entries(params1Headers).forEach(([key, value]) => {
      if (value !== undefined) headers.set(key, value);
    });
    
    Object.entries(params2Headers).forEach(([key, value]) => {
      if (value !== undefined) headers.set(key, value);
    });
    
    return {
      ...this.instance.defaults,
      ...params1,
      ...(params2 || {}),
      headers,
    };
  }

  private createFormData(input: Record<string, unknown>): FormData {
    return Object.keys(input || {}).reduce((formData, key) => {
      const property = input[key];
      formData.append(
        key,
        property instanceof Blob
          ? property
          : typeof property === 'object' && property !== null
          ? JSON.stringify(property)
          : `${property}`,
      );
      return formData;
    }, new FormData());
  }

  public request = async <T = any, _E = any>(
    {
      secure,
      path,
      type,
      query,
      format,
      body,
      ...params
    }: FullRequestParams,
  ): Promise<AxiosResponse<T>> => {
    const secureParams =
      ((typeof secure === 'boolean' ? secure : this.secure) &&
        this.securityWorker &&
        (await this.securityWorker(this.securityData))) ||
      {};
    const requestParams = this.mergeRequestParams(params, secureParams);
    const responseFormat = format || this.format || undefined;

    if (type === ContentType.FormData && body && body !== null && typeof body === 'object') {
      // 确保headers存在并使用AxiosHeaders处理
      if (!requestParams.headers) {
        requestParams.headers = new axios.AxiosHeaders();
      }
      
      // 使用AxiosHeaders的set方法设置headers
      const headers = requestParams.headers instanceof axios.AxiosHeaders 
        ? requestParams.headers 
        : new axios.AxiosHeaders();
      headers.set('Accept', '*/*');
      requestParams.headers = headers;

      body = this.createFormData(body as Record<string, unknown>);
    }

    // 确保headers是AxiosHeaders类型
    const headers = requestParams.headers instanceof axios.AxiosHeaders
      ? requestParams.headers
      : new axios.AxiosHeaders();
    
    // 添加Content-Type (如果需要)
    if (type && type !== ContentType.FormData) {
      headers.set('Content-Type', type);
    }
    
    return this.instance.request({
      ...requestParams,
      headers,
      params: query,
      responseType: responseFormat,
      data: body,
      url: path,
    });
  };
}

/**
 * @title API
 * @version 1.0
 */
export class BaseAPI {
  protected axios: HttpClient<any> = new HttpClient<any>();
}

/**
 * @title API
 * @version 1.0
 */
export class RequiredError extends Error {
  name: 'RequiredError' = 'RequiredError';
  constructor(public field: string, msg?: string) {
    super(msg);
  }
}

export const BASE_PATH = 'http://localhost:8121';

export interface RequestArgs {
  url: string;
  options: AxiosRequestConfig;
}